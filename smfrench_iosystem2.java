import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class iosystem2 {
	
	static final int block_size = 64;
	static final int blocks = 64;
	
	FileChannel fc;

	String filename = "cs143B-2/src/sample_input.txt";
	
	public void open(String filename) {	
		try{ //Open Options p
		OpenOption ooooo[] = new OpenOption[] {StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE};
			this.fc = (FileChannel.open(Paths.get(".",filename), ooooo));
		} catch(IOException e){
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
	public int read_block(int i, byte data[]){
		ByteBuffer copy = ByteBuffer.allocate(block_size);
	    int nread = 0;
	    do {
	        try {
				nread = fc.read(copy);
			} catch (IOException e) {
				e.printStackTrace();
			}
	    } while (nread != -1 && copy.hasRemaining());
		
	    //EOF
	    if(nread == -1){
			data = new byte[block_size];
			
			for(int b = 0; b < block_size; ++b){
				data[b] = 0;
			}
		}
		else {
			data = copy.array();
		}
	    
	    return 0;
	}
	
	public int write_block(int i, byte data[]){
		assert(data.length == block_size);
		ByteBuffer out = ByteBuffer.wrap(data);
		int position = block_size*i;
		try {
			fc.position(position);
			while(out.hasRemaining()){
				fc.write(out);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return 0;
	}
}
