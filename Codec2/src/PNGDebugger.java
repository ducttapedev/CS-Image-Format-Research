import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;


public class PNGDebugger {
	
	private static final String BASE = "TEST";
	private static final File INFILE = new File(BASE + ".png");
	private static final File OUTFILE = new File(BASE + ".txt");
	
	
	public static int toInteger(byte[] b) {
		int result = 0;
		
		for(int i = 0; i < 4; i++) {
			result |= (b[i]&0xFF) << 8*(3-i);
			//System.out.println(result);
		}
		return result;
	}
	
	public static void main(String[] args) throws Exception {
		
		String[] names = {"IHDR", "PLTE", "IDAT", "IEND"};
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(INFILE));
		BufferedWriter writer = new BufferedWriter(new PrintWriter(OUTFILE));
		
		
		byte[] sig = new byte[8];
		in.read(sig);
		writeBytesAsStrings(writer, sig);
		writer.write("\n\n\n");
		byte[] intBytes = new byte[4];
		
		while(true) {
			
			if(in.available() == 0 ) {
				writer.write("UNEXPECTED END");
				break;
			}
			
			// length
			in.read(intBytes);
			System.out.println(Arrays.toString(intBytes));
			int length = toInteger(intBytes);
			writer.write("LENGTH = " + length + "\n");
			
			// type
			in.read(intBytes);
			String type = new String(intBytes);
			writer.write("TYPE = " + type + "\n");
			if(type.equals("IEND"))
				break;
			
			// data
			byte[] data = new byte[length];
			in.read(data);
			writeBytesAsStrings(writer, data);
			writer.write("\n");
			
			// crc
			in.read(intBytes);
			int crc = toInteger(intBytes);
			writer.write("CRC = " + crc + "\n\n\n");
			
		}
		
		writer.close();
		
		
		
		
		
		/*
		// read image
		BufferedImage image = null;
		image = ImageIO.read(INFILE);
		
		// get data
		final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		
		for(int i = 0; i < 1000; i++) {
			System.out.format("%5d", pixels[i]);
			//System.out.print((char)pixels[i]);
		}
		System.out.println();
		
		System.exit(0);
		*/
		
		/*
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		byte[] imageData = byteOut.toByteArray();
		ImageIO.write(image, "png", byteOut);
		
		 */
		
		
		
		/*
		BufferedImage image = null;
		image = ImageIO.read(INFILE);
		ImageIO.write(image, "png", new BufferedOutputStream(new FileOutputStream( new File("blah.png") )));
		
		//System.exit(0);
		*/
		
		
		
		/*
		
		int length = (int) INFILE.length();
		byte[] imageData = new byte[length];
		in.read(imageData);
		
		boolean imageEnd = false;
		int count = 0;
		
		for(int i = 0; !imageEnd; i++) {
			count++;
			
			byte[] temp = new byte[4];
			for(int j = 0; j < 4; j++) {
				temp[j] = imageData[i+j];
			}
			String name = new String(temp);
			
			for(int j = 0; j < names.length; j++) {
				if( name.equals(names[j]) ) {
					count = 0;
					writer.write("\n\n");
					writer.write(names[j] + "\n");
					
					if(name.equals("IEND")) {
						imageEnd = true;
					}
					
					i+=4;
					break;
				}
			}
			
			writer.write( String.format("%5d", imageData[i]) );
			
			if(count > 500) {
				count = 0;
				writer.write("\n");
			}
			//System.out.print((char)imageData[i]);
		}
		
		writer.close();
		*/
	}

	private static void writeBytesAsStrings(BufferedWriter writer, byte[] data)
			throws IOException {
		for(int i = 0; i < data.length; i++) {
			writer.write(String.format("%5d", data[i]));
			
			if(i%20 ==19 && i < data.length-1)
				writer.write("\n");
		}
	}
}
