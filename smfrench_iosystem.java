import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class iosystem {
	
	static final int block_size = 64;
	static final int blocks = 64;
	
	byte data[][];
	FileChannel fc;
	
	public iosystem()
	{
		data = new byte[blocks][];
		for(int i = 0; i < blocks; ++i) data[i] = null;
	}
	public void open(String filename) {	
		try{ //Open Options p
			OpenOption ooooo[] = new OpenOption[] {StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE};
			this.fc = (FileChannel.open(Paths.get(".",filename), ooooo));
		} catch(IOException e){
			System.err.println("Error");
		}
	}
	private boolean hasData(int block)
	{	if(data[block] == null) return false;
		for(int i = 0; i < block_size; ++i)
			if(data[block][i] != 0) return true;
		return false;
	}
	public void save() {
		if(fc == null) return;
		try {
			for(int i = 0; i < blocks; ++i){
				fc.position(i*block_size);
				if(hasData(i)){	
					ByteBuffer B = ByteBuffer.wrap(data[i]);
					while(B.hasRemaining()) fc.write(B);
				}
			}
		}
		catch (IOException e) {
			System.err.println("Error");
		}
	}
	public void close(){
		if(fc.isOpen()){
			try {
				fc.force(false);
				fc.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.err.print("Error");
			}
		}
	}
	
	public byte[] read_block(int i){
		//System.out.printf("REading block %d\n",i);
		byte data[] = this.data[i];
		if(data == null){
			data = new byte[block_size];
			int nread = -1;
			if(fc != null)
				try {
					this.fc.position(i * block_size);
					ByteBuffer B = ByteBuffer.wrap(data);
					do this.fc.read(B); while (nread != -1 && B.hasRemaining());
				}
				catch(IOException e)
				{
					System.err.println("Unrecoverable Error.  Attempting to continue anyway.");
				}
			if(nread == -1) for(int j = 0; j < block_size; ++j) data[j] = 0;
		}
		return data;
	}
	
	public void write_block(int i, byte data[]){
		//System.out.printf("Writing block %d\n",i);
		this.data[i] = data.clone();
	}
}
