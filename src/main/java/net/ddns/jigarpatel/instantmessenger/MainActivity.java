package net.ddns.jigarpatel.instantmessenger;
import android.os.*;
import android.support.design.widget.*;
import android.support.v4.view.*;
import android.support.v4.widget.*;
import android.support.v7.app.*;
import android.support.v7.widget.Toolbar;
import android.util.*;
import android.view.*;
import android.widget.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
	private ListView listView;
	private ImageButton sendButton;
	private String name;
	private TextView header;
	private TextView inputField;
	private int index = 0;
	private Socket socket;
	private PrintWriter output;
	private NavigationView navigationView;
	private BufferedReader input;
	private boolean gotList;
	private String selected = "|";
	private MessageCell adapter;
	private boolean running;
	private ArrayList<Message> list;
	private ArrayList<ArrayList<Message>> messageList;
	private Handler handler = new Handler()
	{
		@Override
		public void handleMessage(android.os.Message msg)
		{
			super.handleMessage(msg);
			String message = (msg.obj).toString();
			if (message.startsWith("message"))
			{
				message = message.substring(8);
				String from = message.substring(0, message.indexOf("|"));
				message = message.substring(message.indexOf("|") + 1);
				if (message.startsWith("|"))//to everyone
				{
					message = message.substring(2);
					messageList.get(0).add(new Message(from, message, false));
					if (selected.equals("|"))
					{
						list.add(new Message(from, message, false));
					}
				}
				else
				{
					message = message.substring(message.indexOf("|") + 1);
					int i = getIndex(from);
					messageList.get(i).add(new Message(from, message, false));
					if (i==index)
					{
						list.add(new Message(from, message, false));
					}
				}
				adapter.notifyDataSetChanged();
				listView.setSelection(listView.getCount() - 1);
			}
			else if (message.startsWith("enter"))
			{
				navigationView.getMenu().add(message.substring(6).substring(0, message.substring(6).indexOf("|")));
				messageList.add(new ArrayList<Message>());
			}
			else if (message.startsWith("leave"))
			{
				try
				{
					message = message.substring(6);
					int i = getIndex(message.substring(0, message.indexOf("|")));
					if (i != 0)
					{
						navigationView.getMenu().getItem(i).setVisible(false);
						messageList.remove(i);
						if (i == index)
						{
							index = 0;
							switchTo(0);
						}
					}
				}
				catch ( Exception e)
				{}
			}
			else if (message.startsWith("appendL"))
			{
				message = message.substring(8);
				String from = message.substring(0, message.indexOf("|"));
				message = message.substring(message.indexOf("|") + 1);
				if (message.startsWith("|"))//to everyone
				{
					message = message.substring(2);
					messageList.get(0).get(messageList.get(0).size() - 1).setMessage(messageList.get(0).get(messageList.get(0).size() - 1).getMessage() + "\n" + message);
					if (selected.equals("|"))
					{
						list.get(list.size() - 1).setMessage(list.get(list.size() - 1).getMessage() + "\n" + message);
					}
				}
				else
				{
					message = message.substring(message.indexOf("|") + 1);
					int i = getIndex(from);
					messageList.get(i).get(messageList.get(i).size() - 1).setMessage(messageList.get(i).get(messageList.get(i).size() - 1).getMessage() + "\n" + message);
					if (i==index)
					{
						list.get(list.size() - 1).setMessage(list.get(list.size() - 1).getMessage() + "\n" + message);
					}
				}
				adapter.notifyDataSetChanged();
				listView.setSelection(listView.getCount() - 1);
			}
		}
	};
	Thread thread = new Thread()
	{
		public void run()
		{
			try
			{
				gotList = false;
				socket = new Socket("jigarpatel.dynu.net", 1234);
				input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				output = new PrintWriter(socket.getOutputStream(), true);
				String text = "";
				while ((text = input.readLine()) != null)
				{
					if (!running)
					{
						break;
					}
					if (!gotList)
					{
						if (!text.equals("|messagelast"))
						{
							navigationView.getMenu().add((text.substring(7, text.length() - 8)));
							messageList.add(new ArrayList<Message>());
						}
						else
						{
							gotList = true;
						}
					}
					else
					{
						android.os.Message message = new android.os.Message();
						message.obj = text;
						handler.sendMessage(message);
					}

				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.setDrawerListener(toggle);
		toggle.syncState();

		navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);
		navigationView.getMenu().add("Everyone");
		inputField = (TextView) findViewById(R.id.inputField);
		listView = (ListView) findViewById(R.id.listView);
		sendButton = (ImageButton) findViewById(R.id.sendButton);
		header = ((TextView) (navigationView.getHeaderView(0).findViewById(R.id.headerText)));
		header.setText("");
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		messageList = new ArrayList<>();
		list = new ArrayList<>();
		messageList.add(new ArrayList<Message>());
		name = null;
		switchTo(0);
		inputField.setHint("Enter Name");
		running = true;
		inputField.setWidth(((RelativeLayout) findViewById(R.id.layout)).getWidth() - 300);
		thread.setDaemon(true);
		thread.start();
		sendButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				String text = inputField.getText().toString();
				if (name == null)
				{
					boolean valid = true;
					for (int i = 0; i < navigationView.getMenu().size(); i++)
					{
						if (navigationView.getMenu().getItem(i).getTitle().toString().equals(text.trim()))
						{
							valid = false;
							break;
						}
					}
					if (valid && !text.trim().equals("") && !text.equals(null) && !text.trim().contains("|"))
					{
						final String t = text.replace('\n', ' ');
						new Thread()
						{
							public void run()
							{
								sendToServer("name|" + t.trim() + "|message");
							}
						}.start();
						name = text.trim();
						header.setText(name);
						//header.setText("asdf");
						//setTitle(getTitle() + " - " + name);
						inputField.setHint("Enter Message");
					}
				} else
				{
					final String t = text;
					new Thread()
					{
						public void run()
						{
							sendToServer("message|" + selected + "|" + t);
						}
					}.start();
					messageList.get(index).add(new Message(name, text, true));
					list.add(new Message(name, text, true));
					listView.setSelection(listView.getCount());
					adapter.notifyDataSetChanged();
				}
				inputField.setText("");
			}
		});
	}
	private void sendToServer(String text)
	{
		try
		{
			Log.d("Send", text);
			output.println(text);
			output.flush();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if (name != null)
		{
			closeConnections();
		}
	}

	@Override
	public void onBackPressed()
	{
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START))
		{
			drawer.closeDrawer(GravityCompat.START);
		}
		else if (name != null)
		{
			inputField.setText("");
			inputField.setHint("Enter Name");
			closeConnections();
			header.setText("");
			list.clear();
			messageList.clear();
			messageList = new ArrayList<>();
			list = new ArrayList<>();
			adapter = new MessageCell(this, list);
			listView.setAdapter(adapter);
			adapter.notifyDataSetChanged();
			navigationView.getMenu().clear();
			index = 0;
			selected = "|";
			adapter.notifyDataSetChanged();
			name = null;
			navigationView.getMenu().add("Everyone");
			messageList.add(new ArrayList<Message>());
			running = true;
			thread.start();
		}
	}
	public void closeConnections()
	{
		if (name != null)
		{
			new Thread()
			{
				public void run()
				{
					sendToServer("leave|||message");
				}
			}.start();

			running = false;
			try
			{
				socket.close();
				input.close();
				output.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		//getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings)
		{
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings("StatementWithEmptyBody")
	@Override
	public boolean onNavigationItemSelected(MenuItem item)
	{
		index = getIndex(item.getTitle().toString());
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		selected = item.getTitle().toString();
		switchTo(index);
		if (selected.equals("Everyone"))
		{
			selected = "|";
		}
		return true;
	}
	private void switchTo(int index)
	{
		list = new ArrayList<>(messageList.get(index));
		adapter = new MessageCell(this, list);
		listView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}
	private int getIndex(String t)
	{
		for (int i = 1; i < navigationView.getMenu().size(); i++)
		{
			if (t.equals(navigationView.getMenu().getItem(i).getTitle().toString()))
			{
				return i;
			}
		}
		return 0;
	}
}

