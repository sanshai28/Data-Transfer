import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;
import java.net.*;
public class Client {    
   
    public static void main(String[] args) {
	
			if (args[0] == null) {
				System.out.println("parameters not match");	
				System.exit(1);
			}
			DatagramSocket Socket = null;
			Header head = new Header();
			ByteArrayOutputStream bos = null;
			ByteArrayInputStream bis = null;
			ObjectOutputStream os = null ;
			ObjectInputStream ois = null;
			byte[] data = null;
			DatagramPacket spacket,ipacket;
			FileOp fil = null;
			byte[] msg = null;
			byte[] imsg = null;
			byte[] obj = null;
		try{
			fil = new FileOp();
			Socket = new DatagramSocket(head.MYTCP_PORT);
            InetAddress IPAddress = InetAddress.getByName(args[0]);	
						
			String file = fil.readFile("myfile.txt");
			long ci=0,lsize = file.length();
			long end,len = 0;
			int ll = 0,sq = 0;
			
			System.out.println("The file length is: "+ lsize + " bytes\n");
			System.out.println("the packet length is: "+head.DATALEN + " bytes\n");
			System.out.println();
			Scanner sc = new Scanner(System.in);
			System.out.print("Enter window size: ");
			int window =sc.nextInt();
			long startTime = System.currentTimeMillis();
				
				while(true)
				{
					LOOP:for(int i=0; (i < window && ci <= lsize) ; i++){
						if(lsize - ci <= head.DATALEN )
							len = lsize - ci;
						else
							len = head.DATALEN;
							System.out.println(" seqnum : "+sq);
							ll = (int)(ci)+(int)(len);
							//System.out.println("Works fine:" +ci+ "::"+ll+":::"+" "+sq);
							StringBuilder tm = new StringBuilder(file.substring((int)ci,ll));
							String tmp = tm.toString();
							//System.out.println("Works fine :" + tmp);
							Packet pack = new Packet();
							pack.seq_num = sq;
							pack.wind = window;
							pack.data = tmp;
							pack.end = lsize == ll;
							pack.ind = !(indicator((int)lsize,(int)ci,(int)len,head.DATALEN));
							
							bos = new ByteArrayOutputStream();	
							os = new ObjectOutputStream(bos);
							os.writeObject(pack);
							msg = bos.toByteArray();
							spacket = new DatagramPacket(msg, msg.length, IPAddress, head.MYUDP_PORT);
							Socket.send(spacket);								
							sq++;
							
						if(sq > 7){
							sq=0;
							}
						ci += len;
											
						os.flush();	
						msg = null;
						tmp = "";
						if(ci == lsize)
							break LOOP;
						//System.gc() 
					}
						imsg = new byte[1024];           
						ipacket = new DatagramPacket(imsg, imsg.length);
						Socket.receive(ipacket);
						obj = ipacket.getData();
						bis = new ByteArrayInputStream(obj);            
						ois = new ObjectInputStream(bis);						
							try {
								Ack ackn = (Ack) ois.readObject();
								 int i=0,rq = ackn.ack_seq;
								 if(rq == 10){
									System.out.println("Suceesfully sent");
									break;
									}
								 //System.out.println("request seq : "+rq );
								 if ((rq > -1)){ //loss
									while(rq != sq)
									{
										i++;
										if(sq == 0)
											sq = 8;
										sq --;	
									}
										if(ci == lsize )
											ci = ci-len-((i-1)*head.DATALEN);				
										else
											ci=ci-((i)*head.DATALEN);
										sq = rq;
								 }
								
								  //int tt=(int)(ci)+(int)(len);
								  //System.out.println("file size sent : "+ci);
								 //else (rq == -1) // no loss
								
							} catch (ClassNotFoundException e) {
								e.printStackTrace();
							}
					
				
				}
				
			long endTime   = System.currentTimeMillis();
			long totalTime = endTime - startTime;
			System.out.println("Time taken : "+ totalTime+" ms");
			System.out.println("Data sent(byte): "+ lsize);;
			System.out.println("Data rate: "+ (double)lsize / totalTime +" (Kbytes/s)");
		}
		catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }// catch (InterruptedException e) {
          //  e.printStackTrace();
       // }
      
    }
	public static boolean indicator(int lsize,int ci,int len,int DATALEN)
	{
	int lenn,c = ci;
		if(lsize - c <= DATALEN )
			lenn = lsize - c;
		else
			lenn = DATALEN;
		c += lenn;
		return lsize == c;

	}

}
