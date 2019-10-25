package net.ddns.jigarpatel.instantmessenger;

import android.content.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MessageCell extends ArrayAdapter<Message>
{
	public MessageCell(Context context, ArrayList<Message> messages)
	{
		super(context, 0, messages);
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		Message message = getItem(position);
		int type = getItemViewType(position);
		if(convertView == null)
		{
			if (type == 1)
			{
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.message_cell, parent, false);
				TextView name = (TextView) convertView.findViewById(R.id.name);
				name.setText(message.getName());
			}
			else
			{
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.message_cell_mine, parent, false);
			}
		}
		TextView messageText = (TextView) convertView.findViewById(R.id.message);
		messageText.setText(message.getMessage());
		return convertView;
	}
	@Override
	public int getViewTypeCount()
	{
		return 2;
	}
	@Override
	public int getItemViewType(int position)
	{
		if(getItem(position).getIsMine())
		{
			return 0;
		}
		else
		{
			return 1;
		}
	}
}
