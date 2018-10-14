import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class test_shell {
	
	public static void main(String args[]){
		test_shell sh = new test_shell();
		
		try{ sh.test();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void test() throws IOException {
		int index = 0;
		int position = 0;
		String name;
		
		filesystem fs = new filesystem();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String line = null;
		
		//create - cr string
		Pattern cr = Pattern.compile("cr ([a-zA-Z][a-zA-Z0-9]{0,3})");
		//destroy - de string
		Pattern de = Pattern.compile("de ([a-zA-Z][a-zA-Z0-9]{0,3})");
		//open - op<name>
		Pattern op = Pattern.compile("op ([a-zA-Z][a-zA-Z0-9]{0,3})");
		//close - cl<index>
		Pattern cl = Pattern.compile("cl ([0-3])");
		//read - rd <index><count>
		Pattern rd = Pattern.compile("rd ([0-3]) ([0-9]+)");
		//write - wr <index><char><count>
		Pattern wr = Pattern.compile("wr ([0-3]) ([a-zA-Z0-9]) ([0-9]+)");
		//seek - sk<index><pos>
		Pattern sk = Pattern.compile("sk ([0-3+]) ([0-9]+)");
		//directory - dr (List the name of all files)
		Pattern dr = Pattern.compile("dr");
		//initialize - in <disk_cont.txt>
		Pattern init = Pattern.compile("in (.*)");
		Pattern init2 = Pattern.compile("in");
		//save - sv <disk_cont.txt>
		Pattern sv = Pattern.compile("sv (.*)");
		//blank line
		Pattern noOp = Pattern.compile("$\\w*^|//");		
		
		while((line = in.readLine()) != null) {
            Matcher mCreate = cr.matcher(line);
            Matcher mInit = init.matcher(line);
            Matcher mInit2 = init2.matcher(line);
            Matcher mDestroy = de.matcher(line);
            Matcher mOpen = op.matcher(line);
            Matcher mClose= cl.matcher(line);
            Matcher mRead = rd.matcher(line);
            Matcher mWrite = wr.matcher(line);
            Matcher mNOOP = noOp.matcher(line);
            Matcher mLSeek = sk.matcher(line);
            Matcher mSave = sv.matcher(line);
            Matcher mDr = dr.matcher(line);

            if(mInit.find()) {
            	name = mInit.group(1);
            	fs.init(name);
            }
            else if(mInit2.find()){
            	fs.init();
            }
            else if(mCreate.find()){
            	name = mCreate.group(1);
            	fs.cr(name);
            }
            else if(mDestroy.find()){
            	name = mDestroy.group(1);
            	fs.de(name);
            }
            else if(mOpen.find()){ 
            	name = mOpen.group(1); 
            	fs.op(name);
            }
            else if(mClose.find()){
            	index = Integer.parseInt(mClose.group(1));
            	fs.cl(index);
            }
            else if(mRead.find()){ 
            	index = Integer.parseInt(mRead.group(1)); 
            	int count = Integer.parseInt(mRead.group(2));
            	fs.rd(index, count);
            }
            else if(mWrite.find()){ 
            	index = Integer.parseInt(mWrite.group(1));
            	String char_ToWrite;
            	int count = Integer.parseInt(mWrite.group(3));
            	char_ToWrite = mWrite.group(2);
            	fs.wr(index, char_ToWrite, count);
            }
            else if(mLSeek.find()){ 
            	index = Integer.parseInt(mLSeek.group(1));
            	position = Integer.parseInt(mLSeek.group(2));
            	fs.sk(index, position);
            }
            else if(mSave.find()){ 
            	name = mSave.group(1);
            	fs.sv(name);
            }
            else if(mDr.find()){
            	fs.dr();
            }
            else if(mNOOP.find()){ 
            	System.out.println(""); 
            }
            else{
            	System.out.print("Error ");
            }
		}
}
}
