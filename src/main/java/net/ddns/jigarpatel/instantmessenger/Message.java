package net.ddns.jigarpatel.instantmessenger;
public class Message
{
	private String name;
	private String message;
	private boolean isMine;

	public Message(String name, String message, boolean isMine)
	{
		this.name = name;
		this.message = message;
		this.isMine = isMine;
	}
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public String getMessage()
	{
		return message;
	}
	public void setMessage(String message)
	{
		this.message = message;
	}
	public boolean getIsMine()
	{
		return isMine;
	}
	public void setIsMine(boolean isMine)
	{
		this.isMine = isMine;
	}
}
