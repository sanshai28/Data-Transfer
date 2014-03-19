import java.io.Serializable;
public class Packet implements Serializable
{
//packet error indicator
public boolean ind;
//the sequence number
public int seq_num;
//the packet data					
public String data;
//enf transfer
public boolean end;
//file size
public long size;
//window
public int wind;
}