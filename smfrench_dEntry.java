
public class dEntry {
	public byte name[];
	public int fd;
	
	public dEntry(){
		this.name = new byte[4];
	}
	public void clear()
	{
		name[0] = -1;
		name[1] = -1;
		name[2] = -1;
		name[3] = -1;
		fd=-1;
	}
}
