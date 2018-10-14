public class openfiletable {
	PackableMemory buffer;
	int index;
	int current_position;
	
	public openfiletable(){
		buffer = new PackableMemory(64);
	}
	
	public openfiletable(int index, int current_position, PackableMemory buffer){
		this.index = index;
		this.current_position = current_position;
		this.buffer = buffer;
	}
}
