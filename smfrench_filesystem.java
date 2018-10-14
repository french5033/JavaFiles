import java.util.Arrays;

public class filesystem {
	iosystem io;
	bitmap BM;
	openfiletable oft[]; //index, current_poition, buffer
	PackableMemory bbm;
	PackableMemory filedescriptors[];
	//0-3, 4-7... mod 
	
	public filesystem(){
		//io = new iosystem();
		BM = null;
		
		oft = new openfiletable[4];
		for(int i = 0; i<4;++i){
			oft[i] = null;
		}
		
		filedescriptors = new PackableMemory[6];
		for(int i = 0; i < 6; ++i){
			filedescriptors[i] = null;
		}
	}
	private int first_OFT(){
		for(int i = 0; i<4; ++i){
			if(oft[i] == null){
				return i;
			}
		}
		System.out.println("Error: No OFTs");
		return -1;
	}
	private void free_OFT(int i){
		oft[i] = null;
	}
	
	public void cr(String filename){
		int f = first_free_FD();
		int d = find_free_dEntry();
		if(f < 0 || d < 0)
			System.err.println("Error.  Unable to cr file");
		dEntry de = get_dEntry(d);
		filedescriptor fd = getfd(f);
		fd.length = 0;
		fd.block[0] = BM.first_free();
		BM.set(fd.block[0]);
		save_bitmap();
		assert(fd.block[1] == -1 && fd.block[2] == -1);
		savefd(f, fd);
		de.fd = f;
		int name_length = Math.min(filename.length(),4);
		for(int i = 0; i < name_length; ++i) de.name[i] = (byte)filename.charAt(i);
		for(int i = name_length; i < 4; ++i) de.name[i] = 0;
		save_dEntry(d, de);

		System.out.println(new String(de.name) + " created");
	}
	
	private void new_dEntry_block(int block_id){
		PackableMemory m = new PackableMemory(64);
		for(int i = 0; i < 64; ++i) m.mem[i] = -1;
		io.write_block(block_id, m.mem);
	}
	private int find_free_dEntry(){
		final byte empty[] = {-1,-1,-1,-1};
		for(int i = 0; i < 24; ++i){
			if(Arrays.equals(empty,get_dEntry(i).name)) return i;
		}
		System.err.println("Error: No free directory entries");
		return -1;
		
	}
	private int find_dEntry(byte name[]){
		//final byte empty[] = {-1,-1,-1,-1};
		for(int i = 0; i < 24; ++i){
			if(Arrays.equals(name,get_dEntry(i).name)) return i;
		}
		
		//System.err.println("Error: No free directory entries");
		return -1;
	}

	private dEntry get_dEntry(int d){	seek(0, d*8);
		dEntry fd = new dEntry();
		fd.name = read(0, 4);
		PackableMemory fde = new PackableMemory(4);
		fde.mem = read(0,4);
		fd.fd = fde.unpack(0);
		return fd;
		
	}
	private void save_dEntry(int d, dEntry fd){
		seek(0, d*8);
		write(0, 4, fd.name);
		PackableMemory fde = new PackableMemory(4);
		fde.pack(fd.fd, 0);
		write(0,4,fde.mem);
	}
	
	public void de(String filename){		
		int name_length = Math.min(filename.length(),4);
		byte name[] = new byte[4];
		for(int i = 0; i < name_length; ++i) name[i] = (byte)filename.charAt(i);
		for(int i = name_length; i < 4; ++i) name[i] = 0;
		int d = find_dEntry(name);
		dEntry de = get_dEntry(d);
		int f = de.fd;
		filedescriptor fd = getfd(f);
		de.clear();
		save_dEntry(d, de);
		for(int i = 0; i < 3; ++i) if(fd.block[i] != -1){
			BM.clear(fd.block[i]);
		}
		save_bitmap();
		fd.clear();
		savefd(f,fd);
		System.out.println(new String(name) + " destroyed");
	}
	
	public int op(String filename){
		int name_length = Math.min(filename.length(),4);
		byte name[] = new byte[4];
		for(int i = 0; i < name_length; ++i) name[i] = (byte)filename.charAt(i);
		for(int i = name_length; i < 4; ++i) name[i] = 0;
		int d = find_dEntry(name);
		if(d == -1){
			System.err.println("No such file: " + new String(name));
			System.exit(-1);
		}
		cr(filename);
		System.out.println("File " + filename + " was created");
		
		dEntry de = get_dEntry(d);
		int f = de.fd;
		filedescriptor fd = getfd(f);
		int ret = first_OFT();
		if(ret >= 0){
			oft[ret] = new openfiletable();
			oft[ret].current_position = 0;
			oft[ret].buffer = new PackableMemory(64);
			oft[ret].buffer.mem = io.read_block(fd.block[0]);
			oft[ret].index = f;
			System.out.println(new String(name) + " opened " + Integer.toString(ret));
		}
		else{
			System.out.println("Error");
			System.err.println("No available OFTs");
		}
		return ret; //Return OFT index or -1
	}
	
	public void cl(int index){
		//write buffer to disk
		int f = oft[index].index;
		filedescriptor fd = getfd(f);
		int current_block = oft[index].current_position/64;
		if(current_block < 3) io.write_block(fd.block[current_block], oft[index].buffer.mem);
		//free OFT entry
		savefd(f, fd);
		free_OFT(index);
		System.out.println(Integer.toString(index) + " closed");
	}

	public int rd(int index, int count){
		byte data[] = null;
		data = read(index, count);
		if(data.length > 0) System.out.println(new String(data));
		else System.out.println();
		return data.length;
	}
	private byte[] read(int index, int count){
		//compute actual count
		int targetEnd = oft[index].current_position + count;
		final filedescriptor fd = getfd(oft[index].index);
		int fileEnd = fd.length;
		//Can we satisfy entire read?
		int actualCount = count;
		if(targetEnd > fileEnd) actualCount = count - (targetEnd-fileEnd);
		targetEnd = oft[index].current_position + actualCount;
		
		//Will we need more blocks?
		int currentEndBlock = oft[index].current_position / 64;
		int targetEndBlock = targetEnd / 64;
		int data_position = 0;
		int to_read = 0;
		
		byte data[] =  new byte[actualCount];
		
		while(currentEndBlock <= targetEndBlock){	
			int begin = oft[index].current_position %64;
			int end = targetEnd%64;
			if(currentEndBlock < targetEndBlock)
				end = 64;
			to_read = end - begin;
			System.arraycopy(oft[index].buffer.mem, begin, data, data_position, to_read);
			data_position += to_read;
			oft[index].current_position += to_read;
			if(currentEndBlock < targetEndBlock){
				//load next block
				//System.err.println("Writing Block " + Integer.toString(currentEndBlock) + " to lba " + Integer.toString(fd.block[currentEndBlock]) );
				io.write_block(fd.block[currentEndBlock], oft[index].buffer.mem);
				if(currentEndBlock == 2) break;
				//System.err.println("Reading Block " + Integer.toString(currentEndBlock+1) + " from lba " + Integer.toString(fd.block[currentEndBlock+1]) );
				oft[index].buffer.mem = io.read_block(fd.block[currentEndBlock+1]);
			}
			++currentEndBlock;
		}
		return data;
	}
	
	public int wr(int index, String mem_area, int count){
		int bytes_written = 0;
		byte data[] = new byte[count];
		for(int i = 0; i < count; ++i) data[i] = (byte)mem_area.charAt(0);
		bytes_written = write(index,count,data);
		System.out.println(Integer.toString(bytes_written) + " bytes written");
		return bytes_written;
	}
	
	private int write(int index, int count, byte data[]){
		//compute actual count
		int targetEnd = oft[index].current_position + count;
		final filedescriptor fd = getfd(oft[index].index);
		int fileEnd = fd.length;
		//Can we satisfy entire read?
		int actualCount = count;
		if(targetEnd > 64*3) actualCount = count - (targetEnd-64*3);
		targetEnd = oft[index].current_position + actualCount;
		
		//Will we need more blocks?
		int currentEndBlock = oft[index].current_position / 64;
		int targetEndBlock = targetEnd / 64;
		int data_position = 0;
		int to_write = 0;

		while(currentEndBlock <= targetEndBlock){	
			int begin = oft[index].current_position %64;
			int end = targetEnd%64;
			if(currentEndBlock < targetEndBlock)
				end = 64;
			to_write = (end - begin);
			System.arraycopy(data, data_position, oft[index].buffer.mem, begin, to_write);
			data_position += to_write;
			oft[index].current_position += to_write;
			if(currentEndBlock < targetEndBlock){
				//load next block
				io.write_block(fd.block[currentEndBlock], oft[index].buffer.mem);
				if(currentEndBlock == 2) break;
				int nextblock = fd.block[currentEndBlock+1];
				if(nextblock == -1){
					nextblock = BM.first_free(fd.block[currentEndBlock]); //Try to allocate sequentially
					//System.err.println("Allocating new block " + Integer.toString(nextblock));
					fd.block[currentEndBlock+1] = nextblock;
					BM.set(fd.block[currentEndBlock+1]);
					save_bitmap();
					savefd(oft[index].index, fd);
				}
				oft[index].buffer.mem = io.read_block(fd.block[currentEndBlock+1]);
			}
			++currentEndBlock;
		}
		fd.length = Math.max(fileEnd, targetEnd);
		savefd(oft[index].index, fd);
		return actualCount;
	}
	public int sk(int index, int position){
		seek(index, position);
		System.out.println("position is " + position);
		return index;
	}

	private void seek (int index, int position){
		if(position > (64 * 3)){	
			System.out.println("Error: Impossible file offset");
			return;
		}
		
		/*if(position == index) 
			System.out.println("Position matches index ");
			System.out.println("Index is: " + oft[index].current_position + " and Position is: " + position);*/
			
		int currentBlock = oft[index].current_position / 64; 
		
		int targetBlock = position / 64;
		filedescriptor fd = getfd(oft[index].index);
		if(position > fd.length){
			System.out.println("Error.  Cannot seek past EOF");
			return;
		}
		if( currentBlock != targetBlock){
			System.out.println("current block: " + currentBlock +  " does not equal target block " + targetBlock);
			
			if(currentBlock != 3) io.write_block(fd.block[currentBlock], oft[index].buffer.mem);
			oft[index].buffer.mem = io.read_block(fd.block[targetBlock]);
		}
		oft[index].current_position = position;
	}
	
	public void dr(){
		byte[] empty = {-1, -1, -1, -1};
		boolean found = false;
		for(int i = 0; i < 24; ++i){
			dEntry de = get_dEntry(i);
			if(!Arrays.equals(empty, de.name)) {
				System.out.println(new String(de.name) + " ");
				found = true;
			}
		}
		if(found) System.out.println();
	}
	
	public void init(){
		io=new iosystem();
		init2();
	}
	
	public void init(String filename){
		io=new iosystem();
		System.err.println("Warning: Init with filename not fully implemented\n");
		io.open(filename);
		init2();
	}
	private void init2(){	
		//read in block 0
		bbm = new PackableMemory(64);
		bbm.mem = io.read_block(0);
		BM = new bitmap();
		BM.BM[0] = bbm.unpack(0);
		BM.BM[1] = bbm.unpack(4);
		BM.last = 64;
		if(BM.get(0)){ // Do we have an existing filesystem?
			//Load directory
			oft[0] = new openfiletable();
			oft[0].index = 0;
			oft[0].current_position = 0;
			oft[0].buffer.mem = io.read_block(7);
			System.out.println("disk restored");
		}
		else {
			//Create new filesystem
			BM.set(0); // Set bitmap
			BM.set(1); // 1-6 File Descriptors
			BM.set(2);
			BM.set(3);
			BM.set(4);
			BM.set(5);
			BM.set(6);
			BM.set(7); // Root directory
			BM.set(8);
			BM.set(9);
			
			//Create new file descriptor
			init_file_descriptors();
			filedescriptor root = getfd(0);
			//System.out.printf("FD for Root before we write it: %d %d %d %d\n", root.length, root.block[0], root.block[1], root.block[2]);
			root.length = 192;
			root.block[0] = 7;
			root.block[1] = 8;
			root.block[2] = 9;
			savefd(0, root);
			new_dEntry_block(7);
			new_dEntry_block(8);
			new_dEntry_block(9);
			save_descriptor(0);
			save_bitmap();
			
			oft[0] = new openfiletable();
			oft[0].index = 0;
			oft[0].current_position = 0;
			oft[0].buffer.mem = io.read_block(7);
			System.out.println("disk initialized");
		}
	}

	private int first_free_FD(){
		for(int i = 1; i < 24; ++i) {
			filedescriptor fd = getfd(i);
			if(fd.length == -1 && fd.block[0] == -1 && fd.block[1] == -1 && fd.block[2] == -1)
				return i;
		}
		return -1;
	}
	private void save_bitmap(){
		bbm.pack(BM.BM[0], 0);
		bbm.pack(BM.BM[1], 4);
		io.write_block(0, bbm.mem);
	}
	private filedescriptor getfd(int fd){
		filedescriptor ret = new filedescriptor();
		
		int block = file_descriptor_block(fd);
		load_descriptor(block);
		int offt = file_descriptor_offset(fd);
		ret.length = filedescriptors[block].unpack(offt);
		ret.block[0] = filedescriptors[block].unpack((offt + 4));
		ret.block[1] = filedescriptors[block].unpack((offt + 8));
		ret.block[2] = filedescriptors[block].unpack((offt + 12));

		return ret;
	}
	private void savefd(int i, filedescriptor fd){
		int block = file_descriptor_block(i);
		int offt = file_descriptor_offset(i);
		filedescriptors[block].pack(fd.length, offt);
		filedescriptors[block].pack(fd.block[0], (offt+4));
		filedescriptors[block].pack(fd.block[1], (offt+8));
		filedescriptors[block].pack(fd.block[2], (offt+12));
		save_descriptor(block);
	}
	private int file_descriptor_block(int filedescriptor){
		return filedescriptor/4;
	}
	private int file_descriptor_offset(int filedescriptor){
		return (filedescriptor*16)%64;
	}
	private void load_descriptor(int d){
		if(filedescriptors[d] == null){
			filedescriptors[d] = new PackableMemory(64);
			filedescriptors[d].mem = io.read_block(d+1);
		}
	}
	private void save_descriptor(int d){
		io.write_block(d+1, filedescriptors[d].mem);
	}
	//Warning, calling this function will result in all files being erased.  Only call during init.
	private void init_file_descriptors(){
		PackableMemory m = new PackableMemory(64);
		for(int i = 0; i < 64; ++i) m.mem[i] = -1;
		for(int i = 1; i < 7; ++i) io.write_block(i, m.mem);
	}
	public void sv(String filename){
		if(oft[0] != null) cl(0);
		if(oft[1] != null) cl(1);
		if(oft[2] != null) cl(2);
		if(oft[3] != null) cl(3);
		io.open(filename);
		io.save();
		io.close();
		System.out.println("disk saved");
	}
}
