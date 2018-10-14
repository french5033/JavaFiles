public class filedescriptor {
	int length;
	//block 0, 1, 2
	int block[] = new int[3];
	public void clear()
	{
		length = -1;
		block[0] = -1;
		block[1] = -1;
		block[2] = -1;
	}
}
