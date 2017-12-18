package ataDateM_cisuM;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Paths;
import java.util.Arrays;

public class Metadata {
	private String artist;
	private String title;
	private String year;
	private int track = 0;
	private int bitrate = -1;
	private String album;

	private String filename;
	private long filesize;
	private long id3v2Tagsize;

	private boolean id3v1 = false;
	private boolean id3v1_ex = false;
	private boolean id3v2 = false;

	public boolean print = false;

	private static byte[] id3v2_4Preamble = {(byte) 'I', (byte) 'D', (byte) '3', 4, 0, 0};

	private static String[] id3v2_3Frames = {"TIT1", "TIT2", "TYER", "TALB", "TRCK"};
	private static String[] id3v2_2Frames = {"TT1",  "TT2",  "TYE",  "TAL",  "TRK"};
	
	private static int bitratessss[][] = { // [version][layer]
			// {invalid, iii, ii, i}
			
			{0, 0, 0, 0}, // version 2.5, no idea what to do here
			
			{-1, -1, -1, -1}, // invalid
			
			{-1, 3, 4, 4}, // version 2
			
			{-1, 0, 1, 2}, // version 1
	};
	
	private static int bitrates[][] = {
			{-1,  -1,  -1,  -1,  -1 },
			{32,  32,  32,  32,  8  },
			{64,  48,  40,  48,  16 },
			{96,  56,  48,  56,  24 },
			{128, 64,  56,  64,  32 },
			{160, 80,  64,  80,  40 },
			{192, 96,  80,  96,  48 },
			{224, 112, 96,  112, 56 },
			{256, 128, 112, 128, 64 },
			{288, 160, 128, 144, 80 },
			{320, 192, 160, 160, 96 },
			{352, 224, 192, 176, 112},
			{384, 256, 224, 192, 128},
			{416, 320, 256, 224, 144},
			{448, 384, 320, 256, 160},
			{-1,  -1,  -1,  -1,  -1 }
	};

	public Metadata (String filename) throws Exception {
		this.filename = filename;
		read(filename);
	}

	public void readBitrate() throws Exception {
		ByteBuffer mistakeFixer = ByteBuffer.allocate(2);
		ByteBuffer bitrateBuffer = ByteBuffer.allocate(1);
		ByteBuffer versionLayerBuffer = ByteBuffer.allocate(1);

		FileChannel fc = (FileChannel.open(Paths.get(filename)));
		int nread;

		if (id3v2) {
			fc.position(id3v2Tagsize);
		}
				
		fc.read(mistakeFixer);
		int offset = 0;
				
		while(mistakeFixer.getShort(0) == 0x0000) {
			mistakeFixer.rewind();
			fc.read(mistakeFixer);
			offset += 2;
		}
		
		if (mistakeFixer.get(0) == 0x00) {
			offset += 1;
		}
		
		fc.position(fc.position() + 1 + offset);
				
		fc.read(versionLayerBuffer);
		fc.read(bitrateBuffer);
		
		int bitrateKey = (bitrateBuffer.get(0) & 0xf0) >> 4;      //0b00001111
		int versionKey = (versionLayerBuffer.get(0) & 0x18) >> 3; //0b00011000
		int layerKey = (versionLayerBuffer.get(0) & 0x06) >> 1;   //0b00000110
		
		if (print) System.out.println("Version: " + versionKey + ", Layer: " + layerKey);
		if (print) System.out.println("Bitrate Key: " + bitrateKey);
		
		int versionLayerKey = bitratessss[versionKey][layerKey];
		if (versionLayerKey == -1) throw new Exception();
		if (versionLayerKey == 0) {
			System.out.println("I can't read this.");
			return;
		}
		
		int bitrate = bitrates[bitrateKey][versionLayerKey];
		if (bitrate == -1) throw new Exception();
		
		this.bitrate = bitrate;
		
		if (print) System.out.println("here is the bitrate: " + bitrate);
	}

	private static byte[] get24BitSize(int size) {
		byte[] bytes = {0, 0, 0, 0};
		// System.out.println(Integer.toBinaryString((0x80 & 0xFF) + 0x100).substring(1));
		bytes[3] = (byte) (~ 0x80 & (byte)(size & 0xff));
		bytes[2] = ((byte) ((~ 0x80 & (byte)(size >> 7))));
		bytes[1] = ((byte) ((~ 0x80 & (byte)(size >> 14))));
		bytes[0] = ((byte) ((~ 0x80 & (byte)(size >> 21))));

		//System.out.println("byte 0:" + bytes[0]);
		return bytes;
	}

	private static int get24BitSize(byte[] bytes) {
		int val = bytes[2] | bytes[1] << 8 | bytes[0] << 16;
		return val;
	}

	private static int get32BitSize(byte[] bytes) {
		int val = bytes[3] | bytes[2] << 8 | bytes[1] << 16 | bytes[0] << 24;
		return val;
	}

	private static int get28BitSize(byte[] bytes) {
		int val = bytes[3] | bytes[2] << 7 | bytes[1] << 14 | bytes[0] << 21;
		return val;
	}

	private void printFrame(ByteBuffer tag, String data, String frameId) {
		tag.put(frameId.getBytes());
		byte[] sizeBytes2 = get24BitSize(data.length() + extra);
		tag.put(sizeBytes2);
		tag.put((byte) 0x0);
		tag.put((byte) 0x0);
		tag.put((byte) 0x3);
		tag.put(data.getBytes());
		tag.put((byte) 0x0);
	}

	private static int extra = 2;

	private byte[] getTagToWrite() {
		int size = 10;

		if (artist != null) {
			size += 10;
			size += artist.length() + extra;
		}
		if (title != null) {
			size += 10;
			size += title.length() + extra;
		}
		if (album != null) {
			size += 10;
			size += album.length() + extra;
		}
		if (year != null) {
			size += 10;
			size += year.length() + extra;
		}
		if (track != 0) {
			size += 10;
			size += Integer.toString(track).length() + extra;
		}
		byte[] sizeBytes = get24BitSize(size - 10);
		//String tag = "ID3";
		ByteBuffer tag = ByteBuffer.allocate(size);

		tag.put(id3v2_4Preamble);
		tag.put(sizeBytes);

		if (artist != null) {
			printFrame(tag, artist, "TIT1");
		}
		if (title != null) {
			printFrame(tag, title, "TIT2");
		}
		if (album != null) {
			printFrame(tag, album, "TALB");
		}
		if (year != null) {
			printFrame(tag, year, "TALB");
		}
		if (track != 0) {
			printFrame(tag, Integer.toString(track), "TRCK");
		}
		// System.out.println(new String(tag.array()));

		return tag.array();
	}

	private void read(String fileName) throws Exception {
		readv1Metadata(fileName);
		readv2Metadata(fileName);
	}

	private boolean readFrame(FileChannel fc, byte version) {
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

			if (size == 0) return false;

			if ((flagsBuffer.get(0)) != 0 || (version > 2 && (flagsBuffer.get(1)) != 0)) {
				System.out.println("!!! the metadata will probably be broken !!!");
				System.out.println(Integer.toBinaryString(flagsBuffer.get(0)));
			}

			String frameId = new String(idBuffer.array());

			if (print) System.out.println("Found a " + frameId + " frame. Size: " + size + ".");

			if (version != 2 && Arrays.asList(id3v2_3Frames).contains(frameId)) {
				long saved_pos = fc.position();
				dataBuffer = ByteBuffer.allocate(size);
				fc.read(dataBuffer);
				fc.position(saved_pos);
				String data = new String(dataBuffer.array()).trim();

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
					track = Integer.parseInt(data.split("/")[0]);
					break;
				}
			} else if (version == 2 && Arrays.asList(id3v2_2Frames).contains(frameId)) {
				long saved_pos = fc.position();
				dataBuffer = ByteBuffer.allocate(size);
				fc.read(dataBuffer);
				fc.position(saved_pos);
				String data = new String(dataBuffer.array()).trim();

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
					track = Integer.parseInt(data.split("/")[0]);
					break;
				}
			}

			if (print) {
				if (size < 200) {
					dataBuffer = ByteBuffer.allocate(size);

					fc.read(dataBuffer);
					System.out.println(new String(dataBuffer.array()));
				} else {
					System.out.println("(Not printing the " + new String(idBuffer.array()) + " frame due to length.)");
				}
			}

			fc.position(pos + 10 + size);

		} catch (IOException e) {
			System.out.println("I/O Exception: " + e);
		}

		return true;
	}

	public void write (String fileName) throws Exception {
		FileChannel fc = null;
		OutputStream os = null;

		ByteBuffer tag = ByteBuffer.allocate((int) 30);

		// System.out.println(id3v2Tagsize);
		fc = FileChannel.open(Paths.get(this.filename));
		fc.read(tag);
		// System.out.println(new String(tag.array()));

		if (id3v2Tagsize > 0) {
			fc.position(id3v2Tagsize);
		}

		os = new FileOutputStream(fileName);

		ByteBuffer bb = ByteBuffer.allocate(1024);

		fc.position(id3v2Tagsize);
		if (id3v1) {
			fc.truncate(filesize - 128);
		}
		if (id3v1_ex) {
			fc.truncate(filesize - 227);
		}

		int length;
		os.write(getTagToWrite());

		while ((length = fc.read(bb)) > 0) {
			os.write(bb.array(), 0, length);
			bb.rewind();
		}
		os.close();
	}

	private void readv2Metadata(String fileName) throws Exception {
		ByteBuffer tagBuffer;
		ByteBuffer sizeBuffer;
		ByteBuffer versionBuffer;

		FileChannel fc = (FileChannel.open(Paths.get(fileName)));
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

			id3v2 = true;
			
			// check size
			fc.position(6);
			nread = fc.read(sizeBuffer);
			int size = get28BitSize(sizeBuffer.array());
			id3v2Tagsize = size;

			if (print) System.out.println("There is a ID3v2." + version + " tag that is " + size + " bytes long.");
			fc.position(10);

			// read the tags
			boolean more = true;
			while (fc.position() < size && more) { // TODO this seems like it's wrong (should be size+10) but it works..?
				// System.out.println("We are at byte " + fc.position());
				more = readFrame(fc, version);
				if (!more) {
					System.out.println("Reached end of frames.");
				}
			}
		}
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public int getTrack() {
		return track;
	}

	public void setTrack(int track) {
		this.track = track;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public String getFilename() {
		return filename;
	}

	public int getBitrate() {
		return bitrate;
	}

	private void readv1Metadata(String fileName) throws Exception {
		ByteBuffer tagBuffer;

		FileChannel fc = (FileChannel.open(Paths.get(fileName)));
		long length = fc.size();

		int nread;

		// check for id3v1 tag
		fc.position(length-128);
		tagBuffer = ByteBuffer.allocate(3);

		do {
			nread = fc.read(tagBuffer);
		} while (nread != -1 && tagBuffer.hasRemaining());

		if ((new String(tagBuffer.array()).equals("TAG"))) {

			id3v1 = true;

			// check for id3v1+ tag
			tagBuffer = ByteBuffer.allocate(4);
			fc.position(length-128-227);

			do {
				nread = fc.read(tagBuffer);
			} while (nread != -1 && tagBuffer.hasRemaining());

			ByteBuffer albumBuffer;
			ByteBuffer artistBuffer;
			ByteBuffer titleBuffer;
			ByteBuffer zeroBuffer = ByteBuffer.allocate(1);
			ByteBuffer trackBuffer = ByteBuffer.allocate(1);
			ByteBuffer yearBuffer = ByteBuffer.allocate(4);

			if ((new String(tagBuffer.array()).equals("TAG+"))) {
				id3v1_ex = true;

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
				fc.position(length - 128 + 63);
				fc.read(albumBuffer);
			}

			fc.position(length - 128 + 93);
			fc.read(yearBuffer);
			fc.position(length - 128 + 125);
			fc.read(zeroBuffer);      
			fc.position(length - 128 + 126);
			fc.read(trackBuffer);      

			artist = new String(artistBuffer.array()).trim();
			year = new String(yearBuffer.array()).trim();
			title = new String(titleBuffer.array()).trim();
			album = new String(albumBuffer.array()).trim();
			if (zeroBuffer.get(0) == 0 && trackBuffer.get(0) != 0) track = trackBuffer.get(0);
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
