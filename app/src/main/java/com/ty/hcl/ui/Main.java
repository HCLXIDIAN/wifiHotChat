package com.ty.hcl.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.ty.hcl.ChatApplication;
import com.ty.hcl.R;
import com.ty.hcl.listener.Listener;
import com.ty.hcl.listener.TCPFileListener;
import com.ty.hcl.listener.inter.IconReceived;
import com.ty.hcl.model.UDPMessage;
import com.ty.hcl.model.User;
import com.ty.hcl.service.ChatService;
import com.ty.hcl.service.ChatService.MyBinder;
import com.ty.hcl.util.Constant;
import com.ty.hcl.util.LocalMemoryCache;
import com.ty.hcl.util.Util;
import com.ty.hcl.widget.PullToRefreshExpandableListView;
import com.ty.hcl.widget.PullToRefreshExpandableListView.OnRefreshListener;


import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

public class Main extends Base implements IconReceived,OnClickListener{
	private PullToRefreshExpandableListView listView;
	
	private List<User> users=new ArrayList<User>();//存在线的用户
	private Map<String, Queue<UDPMessage>> messages;
	MyServiceConnection connection;
	
	public static MyBinder binder;
	private boolean binded;//是否绑定了service
	private MyAdapter adapter;
	private UserBroadcastReceiver receiver=new UserBroadcastReceiver();
	private Map<String, Message> iconMap=new HashMap<String, Message>();
	
	public static final String ACTION_ADD_USER="com.ty.hcl.adduser";
	
	private TCPFileListener fileListener;

    private DrawerLayout drawerLayout;
    TextView viewTip,tv_set;
	private Handler handler=new Handler(){
		public void handleMessage(Message msg) {
			try {
				//刷新头像
				((ImageView)msg.obj).setImageBitmap(Util.getRoundedCornerBitmap(LocalMemoryCache.getInstance().get(msg.getData().getString("key"))));
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		/*Toolbar toolbar = (Toolbar)findViewById(R.id.activity_toolbar);
		setSupportActionBar(toolbar);
		ActionBar actionBar= getSupportActionBar();*/

        initView();
        init();

        fileListener=TCPFileListener.getInstance();
      	if(!fileListener.isRunning()) {
      		try {
				fileListener.open();
				fileListener.setIconReceived(this);
			} catch (IOException e) {
				e.printStackTrace();
			}
      	}

    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	 listView.setAdapter(adapter = new MyAdapter());
    	 adapter.notifyDataSetChanged();
    	 if(fileListener!=null)
    		 fileListener.setOnProgressUpdate(null);
        if(binder != null){
            binder.noticeOnline();
        }
    }


    public void initView(){
        drawerLayout = (DrawerLayout)findViewById(R.id.id_dl);
        listView=(PullToRefreshExpandableListView) findViewById(R.id.main_listview);
        viewTip=(TextView) findViewById(R.id.toptextView);
        tv_set=(TextView) findViewById(R.id.tv_right_set);

    }

    /**
     * 做一些初始化的动作
     */
    private void init(){
    	Intent intent=new Intent(Main.this,ChatService.class);
    	startService(intent);
       	bindService(intent, connection = new MyServiceConnection(), Context.BIND_AUTO_CREATE);
        IntentFilter filter=new IntentFilter(ACTION_ADD_USER);
        registerReceiver(receiver, filter);

        setDrawLayoutEvents();

    	viewTip.setText("WC");
        tv_set.setText("设置");
        tv_set.setVisibility(View.VISIBLE);
        tv_set.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showSettingSelections();
/*
				startActivity(new Intent(Main.this,com.ty.hcl.ui.Set.class));
                overridePendingTransition(R.anim.anim_back_tomain,R.anim.fade);*/
			}
		});
    	listView.setGroupIndicator(getResources().getDrawable(R.drawable.listview_open_selector));
    	listView.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				if(binded){
					unbindService(connection);
					binded=false;//
				}
				Intent intent=new Intent(Main.this,MessageChat.class );
				switch (groupPosition) {
				case 0:
					String ip= ChatApplication.mainInstance.getLocalIp();//获取自己的IP
					if(ip==null){
						showToast("请检测wifi");
						return false;
					}
					intent.putExtra("IP",ip);
					intent.putExtra("DeviceCode", ChatApplication.mainInstance.getDeviceCode());
					intent.putExtra("name", ChatApplication.mainInstance.getMyName());
					break;
				case 1:
					User user=users.get(childPosition);
					intent.putExtra("IP",user .getIp());
					intent.putExtra("DeviceCode", user.getDeviceCode());
					intent.putExtra("name", user.getUserName());
					break;
				case 2:
					intent=new Intent(Main.this,RoomChat.class ); 
				}
				startActivity(intent);//跳转到个人聊天界面
				return false;
			}
		});
    	listView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				new AsyncTask<Void, Void, Void>() {
					protected Void doInBackground(Void... params) {
						try {
							if(binder!=null)
								binder.noticeOnline();//通知通网内的其他用户自己上线了
							Thread.sleep(300);
						} catch (Exception e) {
							e.printStackTrace();
						}
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						listView.onRefreshComplete();
					}

				}.execute();
			}
		});

    }

	private PopupWindow popupWindow = null;
	public void showSettingSelections(){
		if (popupWindow == null){
			popupWindow = new PopupWindow(this);
			popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
			popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
			popupWindow.setFocusable(true);
			popupWindow.setOutsideTouchable(true);
			popupWindow.setBackgroundDrawable(new BitmapDrawable());
			View popupView = getLayoutInflater().inflate(R.layout.popupsettingwindow,null);
			popupView.findViewById(R.id.tv_setting).setOnClickListener(this);
			popupView.findViewById(R.id.tv_open_wifi_hotspot).setOnClickListener(this);
			popupWindow.setContentView(popupView);
			popupWindow.showAsDropDown(tv_set,0,0);
		}
	}

    public void setDrawLayoutEvents(){
        drawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View view, float v) {
				Log.e("Main","v--->"+v);
                View mContent = drawerLayout.getChildAt(0);
                View mMenu= view;
                float scale = 1-v;

                    float leftScale = 1 - 0.3f*scale;
                    float rightScale =(float)(0.6f+0.4*(1-v));
                    AnimatorSet aSet = new AnimatorSet();
                    AnimatorSet aMenuSet = new AnimatorSet();
                    ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(mMenu,"scaleX",leftScale);
                    ObjectAnimator objectAnimator2=  ObjectAnimator.ofFloat(mMenu,"scaleY",leftScale);
                    ObjectAnimator objectAnimator3 =ObjectAnimator.ofFloat(mMenu,"alpha",0.6f+0.4f*(1-scale));
                    aSet.play(objectAnimator1).with(objectAnimator2);
                    aSet.play(objectAnimator2).with(objectAnimator3);

                    ObjectAnimator oa1 = ObjectAnimator.ofFloat(mContent, "translationX",
                            mMenu.getMeasuredWidth()*(1-scale));
                    ObjectAnimator oa2 = ObjectAnimator.ofFloat(mContent,"scaleX",rightScale);
                    ObjectAnimator oa3 = ObjectAnimator.ofFloat(mContent,"scaleY",rightScale);
                    ObjectAnimator oa4 = ObjectAnimator.ofInt(mContent, "pivotX ", 0);
                    ObjectAnimator oa5 = ObjectAnimator.ofFloat(mContent,"pivotY",mMenu.getMeasuredHeight()/2);
                    // mContent.invalidate();
                    aMenuSet.playTogether(objectAnimator1, objectAnimator2
                            , objectAnimator3, oa1, oa2, oa3, oa4, oa5);
                    aMenuSet.setInterpolator(new LinearInterpolator());
                   // aMenuSet.
                    aMenuSet.start();
                  //  aSet.start();

                }


            @Override
            public void onDrawerOpened(View view) {

            }

            @Override
            public void onDrawerClosed(View view) {

            }

            @Override
            public void onDrawerStateChanged(int i) {

            }
        });
    }

    long oldTime;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_BACK){
			long currentTime=System.currentTimeMillis();
			if(currentTime-oldTime<3*1000){
				finish();
			}else{
				showToast("再按一次退出");
				oldTime=currentTime;
			}
		}
		return true;
	}
    
    @Override
    protected void onDestroy() {
      super.onDestroy();
      if(binded)
      	unbindService(connection);
        stopService(new Intent(Main.this,ChatService.class));
      unregisterReceiver(receiver);
      if(fileListener!=null)
  		try {
  			fileListener.close();
  		} catch (IOException e) {
  			e.printStackTrace();
  		}
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.tv_open_wifi_hotspot:
				dealWithopenWifiAp();
				break;
			default:
		}
	}

    public void dealWithopenWifiAp(){

	}

	/**
     * 用来通知刷新列表
     */
    class UserBroadcastReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			if(binder!=null){
				users.clear();
				Set<Entry<String,User>> set=binder.getUsers().entrySet();
				for(Entry<String,User> entry:set)
					users.add(entry.getValue());
				if(adapter==null) {
					adapter=new MyAdapter();
					listView.setAdapter(adapter);
				}
				adapter.notifyDataSetChanged();
			}else {
				unbindService(connection);
				binded=false;
				bindService(new Intent(Main.this,ChatService.class), connection=new MyServiceConnection(), Context.BIND_AUTO_CREATE);
			}
		}
    	
    }
    
   public class MyServiceConnection implements ServiceConnection{
			@Override
      public void onServiceConnected(ComponentName name, IBinder service) {
			binder=(MyBinder) service;
			messages=binder.getMessages();
			binded=true;
      }

			@Override
      public void onServiceDisconnected(ComponentName name) {
      }
    	
    }
   
   class MyAdapter extends BaseExpandableListAdapter{
	   
	   String[] group={"我的设备","在线","聊天室"};

	@Override
	public Object getChild(int arg0, int arg1) {
		return null;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if(convertView==null){
			holder=new ViewHolder();
			convertView=getLayoutInflater().inflate(R.layout.main_listview_child_item, null);
			holder.userName=(TextView) convertView.findViewById(R.id.main_listview_child_item_name);
			holder.ip=(TextView) convertView.findViewById(R.id.main_listview_child_item_ip);
			holder.msgNum=(TextView) convertView.findViewById(R.id.main_listview_child_item_msg_num);
			holder.icon=(ImageView) convertView.findViewById(R.id.main_listview_child_item_icon);
			convertView.setTag(holder);
		}else {
			holder=(ViewHolder) convertView.getTag();
		}
		switch (groupPosition) {
		case 0://自己
			holder.userName.setText(ChatApplication.mainInstance.getMyName());
			holder.ip.setText(ChatApplication.mainInstance.getLocalIp());
			holder.msgNum.setVisibility(View.INVISIBLE);
			Bitmap bitmap=LocalMemoryCache.getInstance().get(com.ty.hcl.ui.Set.iconName);
			if(bitmap==null){
				 bitmap=BitmapFactory.decodeFile(ChatApplication.iconPath+com.ty.hcl.ui.Set.iconName);
				 if(bitmap!=null){
					 holder.icon.setImageBitmap(Util.getRoundedCornerBitmap(bitmap));
					 LocalMemoryCache.getInstance().put(com.ty.hcl.ui.Set.iconName, bitmap);
				 }else {
					 holder.icon.setImageResource(R.drawable.ic_launcher);
				}
			}else{
				holder.icon.setImageBitmap(Util.getRoundedCornerBitmap(bitmap));
			}
				
			break;
		case 1://在线
			User user=users.get(childPosition);
			holder.userName.setText(user.getUserName());
			holder.ip.setText(user.getIp());
			Queue<UDPMessage> msgs=messages.get(user.getIp());
			if(msgs!=null&&msgs.size()>0){
				holder.msgNum.setVisibility(View.VISIBLE);
				holder.msgNum.setText(msgs.size()+"");
			}else {
				holder.msgNum.setVisibility(View.INVISIBLE);
			}
			Bitmap bitmap1=LocalMemoryCache.getInstance().get(user.getDeviceCode());//用设备id来标识唯一头像
			if(bitmap1==null){//内存中没有
				bitmap1=BitmapFactory.decodeFile(ChatApplication.iconPath+user.getDeviceCode());//从硬盘上获取
				if(bitmap1!=null){
					holder.icon.setImageBitmap(Util.getRoundedCornerBitmap(bitmap1));
					LocalMemoryCache.getInstance().put(user.getDeviceCode(), bitmap1);//放进缓存
					if(!user.isRefreshIcon()){//第一次展示则再次请求刷新
						reFreshIcon(user, holder.icon);
					}
				}else {//磁盘也没有，则发送消息获取
					holder.icon.setImageResource(R.drawable.ic_launcher);
					reFreshIcon(user, holder.icon);
				}
			}else {
				holder.icon.setImageBitmap(Util.getRoundedCornerBitmap(bitmap1));
				if(!user.isRefreshIcon()){//第一次展示则再次请求刷新
					reFreshIcon(user, holder.icon);
				}
			}
			break;
		case 2://聊天室
			Bitmap bitmap2=BitmapFactory.decodeResource(getResources(), R.drawable.all_people_icon);
			holder.icon.setImageBitmap(Util.getRoundedCornerBitmap(bitmap2));
			holder.userName.setText("所有");
			holder.ip.setText("接收所有在线人消息");
			msgs=messages.get(Constant.ALL_ADDRESS);
			if(msgs!=null&&msgs.size()>0){
				holder.msgNum.setVisibility(View.VISIBLE);
				holder.msgNum.setText(msgs.size()+"");
			}else {
				holder.msgNum.setVisibility(View.INVISIBLE);
			}
			break;
		}
		return convertView;
	}
	
	/**
	 * 请求图片
	 * @param user
	 * @param view
	 */
	private void reFreshIcon(User user,ImageView view){
		if(binder!=null)
			try {
				UDPMessage message= ChatApplication.mainInstance.getMyUdpMessage("", Listener.REQUIRE_ICON);
				binder.sendMsg(message, InetAddress.getByName(user.getIp()));
				Message msg=handler.obtainMessage();
				msg.obj=view;
				iconMap.put(user.getDeviceCode(), msg);//记录当前位置的ImageView对象
				user.setRefreshIcon(true);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		switch (groupPosition) {
		case 0:
			return 1;
		case 1:
			return users.size();
		case 2:
			return 1;
		}
		return 0;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return null;
	}

	@Override
	public int getGroupCount() {
		return group.length;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return 0;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		ViewHolder holder;
		if(convertView==null){
			holder=new ViewHolder();
			convertView=getLayoutInflater().inflate(R.layout.main_listview_group_item, null);
			holder.userName=(TextView) convertView.findViewById(R.id.txt);
			holder.ip=(TextView) convertView.findViewById(R.id.num);
			convertView.setTag(holder);
		}else {
			holder=(ViewHolder) convertView.getTag();
		}
		holder.userName.setText(group[groupPosition]);
		if(groupPosition==0){
			holder.ip.setText("[1]");
		}else if(groupPosition==1){
			holder.ip.setText("["+users.size()+"]");
		}else if(groupPosition==2){
			holder.ip.setText("[1]");
		}
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
   }
   
   class ViewHolder{
	   TextView userName;
	   TextView ip;//也充当group的在线人数显示
	   TextView msgNum;
	   ImageView icon;
   }

	@Override
	public void iconReceived(String fileName) {
		Message msg=iconMap.get(fileName);
		if(msg!=null){
			Bitmap bitmap=BitmapFactory.decodeFile(ChatApplication.iconPath+fileName);
			if(bitmap!=null){
				LocalMemoryCache.getInstance().put(fileName, bitmap);
				Bundle bundle=new Bundle();
				bundle.putString("key", fileName);
				msg.setData(bundle);
				handler.sendMessage(msg);
			}
		}
	}


	public boolean onCreateOptionMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
}
