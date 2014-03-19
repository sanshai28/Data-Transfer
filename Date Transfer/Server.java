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

public class Server  {

	public static void main(String[] args) {
    
			DatagramSocket socket = null;
			Header head = new Header();
			ByteArrayOutputStream bos = null;
			ByteArrayInputStream bis = null;
			ObjectOutputStream os = null ;
			ObjectInputStream ois = null;
			byte[] imsg = null,smsg = null , obj =null;
			DatagramPacket spacket = null,ipacket = null;
			StringBuilder sb;
			
			Scanner sc = new Scanner(System.in);
			System.out.print("Enter error ratio: ");
			double err_ratio =sc.nextDouble();
						
        try{
            socket = new DatagramSocket(head.MYUDP_PORT);
			Packet pck = null; 			
			int i=0,count =0,lim = 0;
			int seq,error = -1;			
			String msg;
			ArrayList<String> tem = new ArrayList<String>();
			boolean bool;
            while (true) {
				do
				{
					imsg = new byte[1024];
					ipacket = new DatagramPacket(imsg, imsg.length);
					socket.receive(ipacket);
					obj = new byte[1024];
					obj = ipacket.getData();
					bis = new ByteArrayInputStream(obj);
					ois = new ObjectInputStream(bis);
					
					try {
                    pck = (Packet) ois.readObject(); 
					//System.out.println("Works fine : "+ pck.seq_num);						
					}catch (ClassNotFoundException e){
                    e.printStackTrace();
					}
					bool = pck.ind;
					tem.add(pck.data);
					seq = pck.seq_num;
					lim = pck.wind;	
					i++;
					
					
						Random rn = new Random();
						rn.setSeed(System.nanoTime());
						int rand =  rn.nextInt(1000) + 0;
						
						if((err_ratio*1000 >= rand) && (error == -1)){
							error = pck.seq_num;
							count++;
							//System.out.println("Works fine rand: "+ rand + " seq: "+ pck.seq_num);
							}	
				}
				 while(bool && i < lim);
				 i = 0;				
				 bool = true;
				//ois.flush(); 
				//os.flush(); S
                InetAddress IPAddress = ipacket.getAddress();
                int port = ipacket.getPort();
				
                System.out.println("error : "+ error + " seq num : " + seq);	
                if(error == -1 && !pck.end){ // no loss
					Ack ac = new Ack();
					if(seq == 7)
						ac.ack_seq = 0;
					else	
						ac.ack_seq = ++seq;
						
								smsg = new byte[1024];
								bos = new ByteArrayOutputStream();
								os = new ObjectOutputStream(bos);
								os.writeObject(ac);
								smsg = bos.toByteArray();
								spacket = new DatagramPacket(smsg, smsg.length, IPAddress, port);
								socket.send(spacket);
								
				}
				else if(!pck.end){
				
					Ack ac = new Ack();							
								ac.ack_seq = error;
								//System.out.println("Works fine err1 : "+ ac.ack_seq);
								smsg = new byte[1024];
								bos = new ByteArrayOutputStream();
								os = new ObjectOutputStream(bos);
								os.writeObject(ac);		
								smsg = bos.toByteArray();
								spacket = new DatagramPacket(smsg, smsg.length, IPAddress, port);
								socket.send(spacket);
								int t = tem.size();
								//System.out.println("Works fine ss : "+ t);
								while(error != seq)
								{
									if(seq == 0)
										seq = 8;
									seq--;
									tem.remove(--t);
								}
				}
				else if(error == -1 && pck.end){
							Ack ac = new Ack();							
							ac.ack_seq = 10;								
							smsg = new byte[1024];
							bos = new ByteArrayOutputStream();
							os = new ObjectOutputStream(bos);
							os.writeObject(ac);		
							smsg = bos.toByteArray();
							spacket = new DatagramPacket(smsg, smsg.length, IPAddress, port);
							socket.send(spacket);
						System.out.println("server copied data ");
						break;
				}
				else{// last window have lost
							Ack ac = new Ack();							
							ac.ack_seq = error;								
							smsg = new byte[1024];
							bos = new ByteArrayOutputStream();
							os = new ObjectOutputStream(bos);
							os.writeObject(ac);		
							smsg = bos.toByteArray();
							spacket = new DatagramPacket(smsg, smsg.length, IPAddress, port);
							socket.send(spacket);
							int t = tem.size();								
								while(error != seq)
								{
									if(seq == 0)
										seq = 8;
									seq--;
									tem.remove(--t);
								}
						
				}
				error = -1;
				
				os.flush();				
                //System.gc();
				
            }
				sb = new StringBuilder();
					for (String s : tem)
					{
						sb.append(s);
					}
				FileOp file = new FileOp();
				file.Save("myfile1.txt",sb.toString());	

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException i) {
            i.printStackTrace();
        } //catch (InterruptedException e) {
          //  e.printStackTrace();
        //}
		
    }
 
    
}
