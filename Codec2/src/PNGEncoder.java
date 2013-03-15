import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import javax.imageio.ImageIO;

import util.IntegerList;


public class PNGEncoder {
	private static final String FILE_NAME = "pink leopard.jpg";
	private static final String BASE_NAME = FILE_NAME.split("\\.")[0];
	
	private static final File INFILE = new File(FILE_NAME);
	private static final File OUTFILE_JAVA = new File( BASE_NAME + "_JAVA.png" );
	private static final File OUTFILE_MIKE = new File( BASE_NAME + "_MIKE.png" );
	
	public static void main(String[] args) throws IOException {
		
		int[] integers = {0, 1, 127, -128, -127, -2, -1, 129, 130, 255, 256, 257};
		for(int i = 0; i < integers.length; i++)
			System.out.print(integers[i] + "\t");
		
		System.out.println();
		
		for(int i = 0; i < integers.length; i++) {
			System.out.print( (byte)(integers[i]) + "\t");
		}
		
		System.out.print("\n\n");
		
		byte[] b = {0, 1, 127, -128, -127, -2, -1};
		for(int i = 0; i < b.length; i++)
			System.out.print(b[i] + "\t");
		
		System.out.println();
		
		for(int i = 0; i < b.length; i++) {
			System.out.print( (b[i]&0xFF) + "\t");
		}
				
		// read image
		BufferedImage image = null;
		image = ImageIO.read(INFILE);
		
		// get data
		//SimpleColor[][] colors = getSimpleColorArray(image);
		byte[][][] colors = getByteArray(image);
		
		/*
		 * chunks
		 */
		ArrayList<Chunk> chunkList = new ArrayList<Chunk>();
		
		
		
		
		/*
		 *  see whether or not to use palette
		 */
		IntegerList[] colorHashtable = new IntegerList[4096];
		IntegerList hashList = new IntegerList(256);
		int numColors = 0;
		boolean usePalette = true;
		
		// count # of colors
		for(int i = 0; usePalette && i < colors.length; i++) {
			for(int j = 0; j < colors[i].length; j++) {
				byte[] color = colors[i][j];
				int hash = getHash(color);
				int value = getValue(color);
				
				// if new hash, then color is definitely new
				if(colorHashtable[hash] == null) {
					if(numColors == 256) {
						usePalette = false;
						break;
					}
					hashList.add(hash);
					colorHashtable[hash] = new IntegerList( new int[]{value} );
					numColors++;
				}
				// otherwise, check if the color matches any of the existing colors
				else {
					IntegerList colorList = colorHashtable[hash];
					boolean newColor = true;
					
					for(int k = 0; newColor && k < colorList.size(); k++) {
						if(colorList.get(k) == value) {
							newColor = false;
						}
					}
					
					// if its a new color, add it
					if(newColor) {
						if(numColors == 256) {
							usePalette = false;
							break;
						}							
						colorList.add(value);
						numColors++;
					}
				}
			}// for(int j...)
		}// for(int i...)
		
		// header chunk
		byte[] ihdr = new byte[13];
		writeBytes(image.getWidth(), ihdr, 0);
		writeBytes(image.getHeight(), ihdr, 4);
		
		// bit depth
		ihdr[8] = 8;
		
		// color type depends on if a palette is used
		if(usePalette)
			ihdr[9] = 3;
		else ihdr[9] = 2;
		
		// compression and filter method always 0
		ihdr[10] = 0;
		ihdr[11] = 0;
		
		// no interlacing
		ihdr[12] = 0;
		chunkList.add(new Chunk("IHDR", ihdr));
		
		// create the palette block
		if(usePalette) {
			byte[] plte = new byte[numColors*3];
			int index = 0;
			
			// for each hash value
			for(int i = 0; i < hashList.size(); i++) {
				int hash = hashList.get(i);
				IntegerList colorValueList = colorHashtable[hash];
				
				// get each of the values at that hash and convert to RGB 
				for(int j = 0; j < colorValueList.size(); j++) {
					int value = colorValueList.get(j);
					plte[index] = (byte) (value >>> 16);
					plte[index+1] = (byte) ((value >>> 8)&0xFF);
					plte[index+2] = (byte) (value&0xFF);
					index += 3;
				}
			}
			
			chunkList.add(new Chunk("PLTE", plte));
		}
		
		/*
		 * Output 
		 */
		
		Deflater scrunch = new Deflater(9);
		ByteArrayOutputStream outBytes = new ByteArrayOutputStream(1024);
		DeflaterOutputStream compBytes = new DeflaterOutputStream(outBytes, scrunch);
		
		
		
		// compression type
		final int type = 4;
		
		compBytes.write(4);
		compBytes.write( paeth(colors[0]) );
		
		for(int i = 1; i < colors.length; i++) {
			compBytes.write(4);
			compBytes.write( paeth(colors[i], colors[i-1]) );
		}
		compBytes.close();
		
		byte[] compressedIdat = outBytes.toByteArray();
        scrunch.finish();
        
        //byte[] data = new byte[nCompressed];
        chunkList.add(new Chunk("IDAT", compressedIdat));
        chunkList.add(new Chunk("IEND", new byte[]{}));
        
        // write out with java's encoder
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(OUTFILE_JAVA));
        ImageIO.write(image, "PNG", out);
        out.close();
        
        // write out with this encoder
		out = new BufferedOutputStream(new FileOutputStream(OUTFILE_MIKE));
        
        // signature
        byte[]  pngIdBytes = {-119, 80, 78, 71, 13, 10, 26, 10};
        out.write(pngIdBytes);
        
        for(int i = 0; i < chunkList.size(); i++) {
        	Chunk chunk = chunkList.get(i);
        	out.write(toBytes(chunk.length));
        	out.write(chunk.type);
        	out.write(chunk.data);
        	out.write(toBytes(chunk.crc));
        }
        
        out.close();
	}
	
	
	/**
	 * @param orig
	 * @return the <code>Sub</code>-transformed byte array of <code>orig</code>
	 */
	public static byte[] sub(byte[][] orig) {
		byte[] result = new byte[orig.length*3];
		
		for(int i = 0; i < 3; i++)
			result[i] = orig[0][i];
		
		for(int i = 1; i < orig.length; i++) {
			for(int j = 0; j < 3; j++) {
				result[i*3+j] = (byte) (orig[i][j] - orig[i-1][j]);
			}
		}
		return result;
	}
	
	/**
	 * @param orig
	 * @param origAbove
	 * @return the <code>Up</code>-transformed byte array of <code>orig</code>
	 */
	public static byte[] up(byte[][] orig, byte[][] origAbove) {
		byte[] result = new byte[orig.length*3];
		
		for(int i = 0; i < orig.length; i++)
			for(int j = 0; j < 3; j++)
				result[i*3+j] = (byte) (orig[i][j] - origAbove[i][j]);
		
		return result;
	}
	
	
	public static byte[] average(byte[][] orig) {
		byte[] result = new byte[orig.length*3];
		
		for(int i = 0; i < 3; i++)
			result[i] = orig[0][i];
		
		for(int i = 1; i < orig.length; i++)
			for(int j = 0; j < 3; j++)
				result[i*3+j] = (byte)( (orig[i][j]&0xFF) - (orig[i-1][j]&0xFF)/2 );
		
		return result;
	}
	
	/**
	 * @param orig
	 * @param origAbove
	 * @return the <code>Average</code>-transformed byte array of <code>orig</code>
	 */
	public static byte[] average(byte[][] orig, byte[][] origAbove) {
		byte[] result = new byte[orig.length*3];
		
		for(int i = 0; i < 3; i++)
			result[i] = (byte) ( (orig[0][i]&0xFF) - (origAbove[0][i]&0xFF)/2 );
		
		for(int i = 1; i < orig.length; i++)
			for(int j = 0; j < 3; j++) {
				result[i*3+j] = (byte) ( (orig[i][j]&0xFF) - ((orig[i-1][j]&0xFF) + (origAbove[i][j]&0xFF))/2 );
				//System.out.println( (orig[i-1][j]&0xFF) + "\t" + (origAbove[i][j]&0xFF) + "\t" + ((orig[i-1][j]&0xFF) + (origAbove[i][j]&0xFF))/2 );
			}
		
		return result;
	}
	
	public static byte[] paeth(byte[][] orig, byte[][] origAbove) {
		byte[] result = new byte[orig.length*3];
		
		for(int j = 0; j < 3; j++) {
			result[j] = (byte) ( (orig[0][j]&0xFF) - paethPredictor((byte)0, origAbove[0][j], (byte)0) );
		}
		
		for(int i = 1; i < orig.length; i++)
			for(int j = 0; j < 3; j++) {
				result[i*3+j] = (byte)( (orig[i][j]&0xFF) - paethPredictor((orig[i-1][j]&0xFF), (origAbove[i][j]&0xFF), (origAbove[i-1][j]&0xFF)) );
			}
		
		return result;
	}
	
	public static byte[] paeth(byte[][] orig) {
		byte[] result = new byte[orig.length*3];
		
		for(int j = 0; j < 3; j++) {
			result[j] = orig[0][j];
		}
		
		for(int i = 1; i < orig.length; i++)
			for(int j = 0; j < 3; j++) {
				result[i*3+j] = (byte) ( (orig[i][j]&0xFF) - paethPredictor(orig[i-1][j]&0xFF, (byte)0, (byte)0) );
			}
		
		return result;
	}
	
	/**
	 * returns the PaethPredictor for the 3 bytes
	 */
	private static int paethPredictor(int a, int b, int c) {
		int p = a + b - c;
		int pa = abs(p-a);
		int pb = abs(p-b);
		int pc = abs(p-c);
		
		if(pa <= pb && pa <= pc)
			return a;
		if(pb <= pc)
			return b;
		return c;
	}
	
	public static int abs(int val) {
		return val < 0 ? -val:val;
	}
	
	
	
	
	private static class Chunk {
		public final int length;
		public final byte[] type;
		public final byte[] data;
		public final int crc;
		
		public Chunk(String typeName, byte[] data) {
			length = data.length;
			type = typeName.getBytes();
			assert type.length == 4;
			
			this.data = data;
			CRC32 crc32 = new CRC32();
			crc32.update(this.type);
			crc32.update(data);
			crc = (int) (crc32.getValue());
		}
	}
	
	private static class SimpleColor {
		public byte red, green, blue;
		
		public SimpleColor(byte red, byte green, byte blue) {
			this.red = red;
			this.green = green;
			this.blue = blue;
		}
		
		public int getValue() {
			return red << 16 + green << 8 + blue;
		}
		
		/**
		 * Returns a value from 0~4096<br>
		 * We choose this hash value because it is quick and because it uses the 4 least significant bits.<br>
		 * It is unlikely that there will be a high number of collisions because
		 * that would require that the components differ by a multiple of 16,
		 * which is not particularly probabilistically favorable.<br>
		 * On the other hand, differing by 1 is quite favorable due to smooth gradients.
		 */
		public int getHash() {
			return (red&0xF << 8 + green&0xF << 4 + blue&0xF);
		}
		
		public String toString() {
			return String.format("%35d%35d%35d", red, green, blue);
		}
		public String toBinaryString() {
			return String.format("%35s%35s%35s", Integer.toBinaryString(red), Integer.toBinaryString(green), Integer.toBinaryString(blue) );
		}
	}
	
	/**
	 * Returns the hash of the RGB
	 */
	public static int getHash(byte[] b) {
		assert b.length == 3;
		return (b[0]&0xF << 8 + b[1]&0xF << 4 + b[2]&0xF);
	}
	
	/**
	 * Returns the int value of the RGB
	 */
	public static int getValue(byte[] b) {
		return b[0] << 16 + b[1] << 8 + b[2];
	}

	public static void writeBytes(int integer, byte[] array, int offset) {
		for(int i = 0; i < 4; i++) {
			array[offset + i] = (byte) ( integer >>> ((3-i)*8) );
		}
	}
	
	public static byte[] toBytes(int integer) {
		byte[] b = new byte[4];
		
		for(int i = 0; i < 4; i++) {
			b[i] = (byte) ( integer >>> ((3-i)*8) );
		}
		
		return b;
	}
	
	
	/**
	 * Returns an array of the format [row][col][component], assuming RGB with no alpha.<br>
	 * No alpha is assumed since JPEG does not support alpha.
	 */
	private static byte[][][] getByteArray(BufferedImage image) {

		// get the data
		final int bytesPerPixel = 3;
		final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		final int width = image.getWidth();
		final int height = image.getHeight();
		final boolean hasAlphaChannel = image.getAlphaRaster() != null;
		assert !hasAlphaChannel;
  
		// fill in the array
		byte[][][] result = new byte[height][width][bytesPerPixel];
		
		for (int index = 0, row = 0, col = 0; index < pixels.length; index += bytesPerPixel) {
			for(int i = 0; i < bytesPerPixel; i++)
				result[row][col][i] = pixels[index + 2 - i];
			
			col++;
			if (col == width) {
				col = 0;
				row++;
			}
		}
     	return result;
	}

	
	/**
	 * Returns an array of <code>SimpleColor</code>s in the format [row][col], assuming RGB with no alpha.<br>
	 * No alpha is assumed since JPEG does not support alpha.
	 */
	private static SimpleColor[][] getSimpleColorArray(BufferedImage image) {
		
		// get the data
		final int bytesPerPixel = 3;
		final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		final int width = image.getWidth();
		final int height = image.getHeight();
		final boolean hasAlphaChannel = image.getAlphaRaster() != null;
		assert !hasAlphaChannel;
  
		// fill in the array
		SimpleColor[][] result = new SimpleColor[height][width];
		
		for(int index = 0, row = 0, col = 0; index < pixels.length; index += bytesPerPixel) {
			
			// convert bytes to integers
			result[row][col] = new SimpleColor( pixels[index+2], pixels[index+1], pixels[index] );
			
			col++;
			if (col == width) {
				col = 0;
				row++;
			}
		}
     	return result;
	}
	
	
	
}
