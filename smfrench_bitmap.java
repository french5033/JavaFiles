public class bitmap {
	//Return 0 or 1
	public int last;
	public int BM[];
	
	public static void main(String args[]){
		bitmap B = new bitmap();
		
		B.set(0);
		B.set(1);
		B.set(2);
		B.set(3);
		B.set(4);
		B.set(5);
		B.set(6);
		B.set(10);
		B.set(20);
		B.set(30);
		B.set(4);
		B.set(7);
		B.set(27);
		B.set(22);
		B.set(63);

		for(int i = 60; i<=63; ++i){
			B.set(i);
		}
		
		String S0 = String.format("%32s", Integer.toBinaryString(B.BM[0])).replace(' ', '0');
		String S1 = String.format("%32s", Integer.toBinaryString(B.BM[1])).replace(' ', '0');
		
		System.out.println(S0 + S1);
		
		System.out.println(B.first_free(60));
		
		for(int i = 0; i<B.last; ++i){
			B.set(i);
		}
		for(int i = 0; i<B.last; i+=2){
			B.clear(i);
		}
		
		S0 = String.format("%32s", Integer.toBinaryString(B.BM[0])).replace(' ', '0');
		S1 = String.format("%32s", Integer.toBinaryString(B.BM[1])).replace(' ', '0');
		
		System.out.println(S0 + S1);	
	}
	
	//Default Constructor
	public bitmap(){
		BM = new int[2];
		last = 64;
	}
	
	//Constructor 2
	public bitmap(int BM[]){
		this.BM = BM;
		this.last = BM.length * 32;
	}
	
	//integer parameter
	public void set(int index){
		// ~: Bitwise not
		BM[index/32] = BM[index/32]|(1<<(32-1)-(index%32)); 
	}
	
	public void clear(int index){
		BM[index/32] = BM[index/32]&~(1<<(32-1)-(index%32)); 
	}
	
	public boolean get(int index){
		// Is bit set or not set?
		return 0!=(BM[index/32] & (1<<(32-1)-(index%32)));
	}
	
	public int first_free(){
		return first_free(1);
	}
	
	public int first_free(int hint){ 
		for(int i = hint; i < last; ++i){
			if(!get(i)) return i; 
		}
		for(int i = 0; i<hint && i<last; ++i){
			if(!get(i)) return i;
		}
		System.err.println("No free blocks");
		System.exit(-1);
		return -1;
	}

}