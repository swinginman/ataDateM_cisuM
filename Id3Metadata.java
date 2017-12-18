package ataDateM_cisuM;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Arrays;

public class Id3Metadata {
	public String artist;
	public String title;
	public String year;
	public String track;
	public String album;
	
	public boolean print = false;
			
	public static String[] id3v23Frames = {"TIT1", "TIT2", "TYER", "TALB", "TRCK"};
	public static String[] id3v22Frames = {"TT1",  "TT2",  "TYE",  "TAL",  "TRK"};
	
	public static int get24BitSize(byte[] bytes) {
		int val = bytes[2] | bytes[1] << 8 | bytes[0] << 16;
				
		return val;
	}

	public static int get32BitSize(byte[] bytes) {
		int val = bytes[3] | bytes[2] << 8 | bytes[1] << 16 | bytes[0] << 24;
				
		return val;
	}

	public static int get28BitSize(byte[] bytes) {
		int val  = bytes[3] | bytes[2] << 7 | bytes[1] << 14 | bytes[0] << 21;
				
		return val;
	}
	
	public void read(String fileName) {
		readv1Metadata(fileName);
		readv2Metadata(fileName);
	}
	
	public void readFrame(FileChannel fc, byte version) {
        ByteBuffer sizeBuffer;
        ByteBuffer idBuffer;
        ByteBuffer dataBuffer;
        ByteBuffer flagsBuffer;
        
        int frameIdSize = 4;
        
        if (version == 2) {
        	frameIdSize = 3;
        }
        
		try {
			long pos = fc.position();
			
        	idBuffer = ByteBuffer.allocate(frameIdSize);
        	if (version == 2) {
        		sizeBuffer = ByteBuffer.allocate(3);
            	flagsBuffer = ByteBuffer.allocate(1);
        	} else {
        		sizeBuffer = ByteBuffer.allocate(4);        		
            	flagsBuffer = ByteBuffer.allocate(2);
        	}
        	
        	fc.read(idBuffer);
        	fc.read(sizeBuffer);
        	fc.read(flagsBuffer);
        	
        	int size;
        	if (version == 2) {
        		size = get24BitSize(sizeBuffer.array());
        	} else if (version == 3) {
        		size = get32BitSize(sizeBuffer.array());
        	} else { // version must be 4
        		size = get28BitSize(sizeBuffer.array());
        	}
        	
        	if ((flagsBuffer.get(0)) != 0 || (version > 2 && (flagsBuffer.get(1)) != 0)) {
        		System.out.println("!!! the metadata will probably be broken !!!");
        		System.out.println(Integer.toBinaryString(flagsBuffer.get(0)));
        	}
        	
        	String frameId = new String(idBuffer.array());
        	
        	if (print) System.out.println("Found a " + frameId + " frame. Size: " + size + ".");
        	
        	if (version != 2 && Arrays.asList(id3v23Frames).contains(frameId)) {
        		long saved_pos = fc.position();
	        	dataBuffer = ByteBuffer.allocate(size);
	        	fc.read(dataBuffer);
	        	fc.position(saved_pos);
	        	String data = new String(dataBuffer.array()).trim();
	        	
	        	//if (data.charAt(0) == 3) { // I guess some people like to put 0x3 at the beginning of their frames for some reason???
	        	//	data = data.substring(1);
	        	//}
        		
	        	switch (frameId) {
        		case "TIT1": // artist
        			artist = data;
        			break;
        		case "TIT2": // title
        			title = data;
        			break;
        		case "TYER":
        			year = data;
        			break;
        		case "TALB":
        			album = data;
        			break;
        		case "TRCK":
        			track = data;
        			break;
        		}
        	} else if (version == 2 && Arrays.asList(id3v22Frames).contains(frameId)) {
        		long saved_pos = fc.position();
	        	dataBuffer = ByteBuffer.allocate(size);
	        	fc.read(dataBuffer);
	        	fc.position(saved_pos);
	        	String data = new String(dataBuffer.array()).trim();
	        	
	        	//if (data.charAt(0) == 3) { // I guess some people like to put 0x3 at the beginning of their frames for some reason???
	        	//	data = data.substring(1);
	        	//}

	        	switch (frameId) {
        		case "TT1": // artist
        			artist = data;
        			break;
        		case "TT2": // title
        			title = data;
        			break;
        		case "TYR":
        			year = data;
        			break;
        		case "TAL":
        			album = data;
        			break;
        		case "TRK":
        			track = data;
        			break;
        		}
        	}
        	
        	if (size < 200) {
	        	dataBuffer = ByteBuffer.allocate(size);
	        	
	        	fc.position(fc.position());
	        	
	        	fc.read(dataBuffer);
	        	if (print) System.out.println(new String(dataBuffer.array()));
        	} else {
        		if (print) System.out.println("(Not printing the " + new String(idBuffer.array()) + " frame due to length.)");
        	}
        	
        	fc.position(pos + 10 + size);
        	
		} catch (IOException e) {
			System.out.println("I/O Exception: " + e);
		}
	}
	
	public void readv2Metadata(String fileName) {
        ByteBuffer tagBuffer;
        ByteBuffer sizeBuffer;
        ByteBuffer versionBuffer;

        try (FileChannel fc = (FileChannel.open(Paths.get(fileName)))) {
            int nread;
            
        	// check for id3v2 tag            
            tagBuffer = ByteBuffer.allocate(3);
            nread = fc.read(tagBuffer);
            
            // check what version it is (id32.2, id32.3, id32.4)
            versionBuffer = ByteBuffer.allocate(1);
            nread = fc.read(versionBuffer);
            byte version = versionBuffer.get(0);

            sizeBuffer = ByteBuffer.allocate(4);            
            
            // ensure this is the right tag and it's a version we understand
            if (new String(tagBuffer.array()).equals("ID3") && version >= 1 && version <= 4 ) {
            	
            	// check size
            	fc.position(6);
            	nread = fc.read(sizeBuffer);
            	int size = get28BitSize(sizeBuffer.array());
            	
            	if (print) System.out.println("There is a ID3v2." + version + " tag that is " + size + " bytes long.");
            	fc.position(10);
            	
            	// read the tags
            	while (fc.position() < size) { // TODO this seems like it's wrong (should be size+10) but it works..?
            		// System.out.println("We are at byte " + fc.position());
            		readFrame(fc, version);
            	}
            }
        } catch (IOException e) {
        	System.out.println("I/O Exception: " + e);
        }
   	}
	
    public void readv1Metadata(String fileName) {
        ByteBuffer tagBuffer;

        try (FileChannel fc = (FileChannel.open(Paths.get(fileName)))) {
            long length = fc.size();
            int nread;

        	// check for id3v1 tag
            fc.position(length-128);
            tagBuffer = ByteBuffer.allocate(3);
        	
            do {
                nread = fc.read(tagBuffer);
            } while (nread != -1 && tagBuffer.hasRemaining());
            
            if ((new String(tagBuffer.array()).equals("TAG"))) {
                // check for id3v1+ tag
                tagBuffer = ByteBuffer.allocate(4);
                fc.position(length-128-227);
                
                do {
                    nread = fc.read(tagBuffer);
                } while (nread != -1 && tagBuffer.hasRemaining());
                                
            	ByteBuffer albumBuffer;
            	ByteBuffer artistBuffer;
            	ByteBuffer titleBuffer;
            	ByteBuffer trackBuffer = ByteBuffer.allocate(1);
            	ByteBuffer yearBuffer = ByteBuffer.allocate(4);

            	if ((new String(tagBuffer.array()).equals("TAG+"))) {
                	if (print) System.out.println("There is an extended ID3v1 section.");
                	
                	// read the tag
                	albumBuffer = ByteBuffer.allocate(60);
                	artistBuffer = ByteBuffer.allocate(60);
                	titleBuffer = ByteBuffer.allocate(60);
                	
                	fc.position(length - 128 - 227 + 4);
                	fc.read(titleBuffer);
                	fc.position(length - 128 - 227 + 64);
                	fc.read(artistBuffer);
                	fc.position(length - 128 - 227 + 124);
                	fc.read(albumBuffer);
                	
                } else {
                	if (print) System.out.println("There is a ID3v1 section.");
                	
                	albumBuffer = ByteBuffer.allocate(30);
                	artistBuffer = ByteBuffer.allocate(30);
                	titleBuffer = ByteBuffer.allocate(30);

                	// read the tag
                	fc.position(length - 128 + 3);
                	fc.read(titleBuffer);
                	fc.position(length - 128 + 33);
                	fc.read(artistBuffer);
                	fc.position(length - 128 + 64);
                	fc.read(albumBuffer);
                }
            	
            	fc.position(length - 128 + 93);
            	fc.read(yearBuffer);
            	fc.position(length - 128 + 126);
            	fc.read(trackBuffer);      
            	
            	artist = new String(artistBuffer.array()).trim();
            	year = new String(yearBuffer.array()).trim();
            	title = new String(titleBuffer.array()).trim();
            	album = new String(albumBuffer.array()).trim();
            	track = new String(trackBuffer.array()).trim();
            }
                   
        } catch (IOException e) {
            System.out.println("I/O Exception: " + e);
        }
    }
    
    @Override
    public String toString() {
    	String result = "";
		if (album != null && !album.equals("")) result += "Album: " + album + ", ";
		if (artist != null && !artist.equals("")) result += "Artist: " + artist + ", ";
		if (year != null && !year.equals("")) result += "Year: " + year + " ";
		if (album != null && !album.equals("")) result += "Track: " + track + ", ";
		if (title != null && !title.equals("")) result += "Title: " + title + ", ";
		
		if (result.equals("")) {
			result = "No metadata., ";
		}
		
		return result.substring(0, result.length() - 2);
    }
}
