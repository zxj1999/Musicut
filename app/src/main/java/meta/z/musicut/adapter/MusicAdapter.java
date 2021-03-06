package meta.z.musicut.adapter;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.support.v7.widget.*;
import android.transition.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import java.io.*;
import java.util.*;
import meta.z.musicut.*;
import meta.z.musicut.bean.*;
import meta.z.musicut.manager.*;
import meta.z.musicut.util.*;
import meta.z.musicut.widget.*;
import android.net.*;
import android.media.*;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> implements SongManager.GroupDescriptor
{
	private Context context;
	private ArrayList<ItemInfo> itemInfoList;
	private RecyclerView rvMusic;
	private Transition transition=new ChangeBounds();
	private SimultaneousAnimator animator=new SimultaneousAnimator();
    private class ItemInfo
	{   
	    public static final int TYPE_ITEM_SONG=0;
		public static final int TYPE_ITEM_HEADER=1;

		Song song;
		String description;
		boolean expand;
		boolean collapz;
		int type;
		ItemInfo(Song song, String des, int type)
		{   this.song = song;
			this.description = des;
			this.type = type;
		}
		ItemInfo(String des)
		{
			this.description = des;
			type = TYPE_ITEM_HEADER;
		}
	}

	public void clearItemInfoList()
	{
		itemInfoList.clear();	
	}

	private OnTouchListener touchEater=new OnTouchListener(){
		@Override
		public boolean onTouch(View p1, MotionEvent p2)
		{
			return true;
		}


	};
	public MusicAdapter(Context context, RecyclerView rv)
	{
		this.context = context;
		this.rvMusic = rv;

		transition.addListener(new TransitionUtils.TransitionListenerAdapter(){
				public void onTransitionStart(Transition p1)
				{
					rvMusic.setOnTouchListener(touchEater);
				}
				public void onTransitionEnd(Transition p1)
				{
					rvMusic.setOnTouchListener(null);
					animator.setAnimateMoves(true);
				}
			}).setDuration(200).setInterpolator(AnimUtils.getFastOutSlowInInterpolator());
		rvMusic.setItemAnimator(animator);
		itemInfoList = new ArrayList<ItemInfo>();
		//SongManager.sort(SongManager.SORT_BY_TITLE, SongManager.ORDER_ASCENDING);
		SongManager.setSongGroupDescriptor(this);
		SongManager.refreshDescription();
	}



	@Override
	public void onDescribe(Song song, String des)
	{
		if (song.postion == 0)
		{
			itemInfoList.add(new ItemInfo(des));
		}
		else
		{
			if (!des.equals(itemInfoList.get(itemInfoList.size() - 1).description))
			{
				itemInfoList.add(new ItemInfo(des));
			}
		}

		itemInfoList.add(new ItemInfo(SongManager.cur_song_list.get(song.postion), des, ItemInfo.TYPE_ITEM_SONG));

	}

	@Override
	public int getItemViewType(int position)
	{
		return itemInfoList.get(position).type;
	}


	@Override
	public MusicAdapter.MusicViewHolder onCreateViewHolder(ViewGroup p1, int p2)
	{
		if (p2 == ItemInfo.TYPE_ITEM_SONG)
		{
			return new MusicViewHolder(LayoutInflater.from(context).inflate(R.layout.item_song, p1, false), p2);
		}
		return new MusicViewHolder(LayoutInflater.from(context).inflate(R.layout.item_header, p1, false), p2);
	}

	private void expandItem(MusicViewHolder holder, boolean shouldExpand)
	{
		itemInfoList.get(holder.getAdapterPosition()).expand = shouldExpand;
		notifyItemChanged(holder.getAdapterPosition(), shouldExpand);
	}

	@Override
	public void onBindViewHolder(MusicAdapter.MusicViewHolder holder, int position, List<Object> payloads)
	{
		if (payloads.size() == 0)
		{
			onBindViewHolder(holder, position);
			return;
		}
		if (payloads.contains(true))
		{
			holder.itemView.setActivated(true);
			UiUtils.visible(holder.llOption);
		}
		else
		{
			holder.itemView.setActivated(false);
			UiUtils.gone(holder.llOption);
		}
	}


	@Override
	public void onBindViewHolder(final MusicAdapter.MusicViewHolder p1, final int p2)
	{ final ItemInfo itemInfo=itemInfoList.get(p2);
		switch (getItemViewType(p2))
		{
			case ItemInfo.TYPE_ITEM_SONG:
				final Song song=itemInfo.song;
				if (itemInfo.collapz)
				{
					UiUtils.gone(p1.rlInfo, p1.llOption);
					return;
				}
				UiUtils.visible(p1.rlInfo);
				p1.tvSongTitle.setText(song.title);
				p1.tvSongInfo.setText(song.artist + " - " +song.album+" - "+ MusicUtils.formatSongDuration(song.duration));
				p1.civAlbumArt.setImageResource(R.mipmap.ic_default_album_art);
				new Thread(new Runnable(){
						@Override
						public void run()
						{   
							final Bitmap mp=MusicUtils.getArtwork(context, song.song_id, song.album_id);		
							((Activity)context).runOnUiThread(new Runnable(){
									@Override
									public void run()
									{ 
										if (mp != null)
										{
											p1.civAlbumArt.setImageBitmap(mp);
										}
									}
								});
						}
					}).start();
				if (itemInfo.expand)
				{
					p1.itemView.setActivated(true);
					UiUtils.visible(p1.llOption);
				}
				else
				{
     				p1.itemView.setActivated(false);
					UiUtils.gone(p1.llOption);
				}
				break;

			case ItemInfo.TYPE_ITEM_HEADER:
				p1.tvHeader.setText(itemInfo.description);

				if (itemInfo.collapz)
				{
					p1.ivChevron.setRotation(-90);
				}
				else
				{
					p1.ivChevron.setRotation(0);
				}

				break;
		}
	}


	@Override
	public int getItemCount()
	{
	    return  itemInfoList.size();
	}




	public class MusicViewHolder extends RecyclerView.ViewHolder
	{   
	    //普通条目
	    private TextView tvSongTitle;
		private TextView tvSongInfo;
		private CircularImageView civAlbumArt;
		private View llOption, rlInfo;
        private Button btnItemSongPlay,btnItemSongCut,btnItemSongDetail;	
		private ImageButton ibEdit;
		//header条目
		private TextView tvHeader;
		private ImageView ivChevron;
		//播放器

		public MusicViewHolder(View item, int itemType)
		{
			super(item);
			final MusicViewHolder viewHolder=MusicViewHolder.this;

			if (itemType == ItemInfo.TYPE_ITEM_SONG)
			{
				tvSongTitle = (TextView) item.findViewById(R.id.tv_song_title);
				tvSongInfo = (TextView) item.findViewById(R.id.tv_song_info);
				civAlbumArt = (CircularImageView) item.findViewById(R.id.item_song_civ_album_art);
				llOption = item.findViewById(R.id.rl_option_container);
				rlInfo = item.findViewById(R.id.rl_main);
				btnItemSongCut = (Button) item.findViewById(R.id.item_song_btn_cut);
				btnItemSongPlay = (Button) item.findViewById(R.id.item_song_btn_play);
				btnItemSongDetail=(Button) item.findViewById(R.id.item_song_btn_detail);
				ibEdit = (ImageButton) item.findViewById(R.id.ib_edit);

				btnItemSongCut.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View p1)
						{
                         context.startActivity(new Intent(context,MusicutActivity.class));
						}
					});

				btnItemSongPlay.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View p1)
						{
							Intent i=new Intent(context, MusicPlayerActivity.class);
							Bundle bundle=ActivityOptions.makeSceneTransitionAnimation((Activity)context, new Pair[]
																					   {    Pair.create(civAlbumArt, "transition_album_art")
																						   ,Pair.create(tvSongTitle, "transition_song_title")
																						   ,Pair.create(tvSongInfo, "transition_song_info")
																						   ,Pair.create(itemView, "transition_dialog")}).toBundle();
							Song song=itemInfoList.get(getAdapterPosition()).song;
							if (!song.exists())
							{
								SnackBar.build(context, R.string.song_not_exists, android.R.string.ok)
									.show(1000);
								return;
							}
							i.putExtra("song", song);
							context.startActivity(i, bundle);

						}
					});
					
				btnItemSongDetail.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View p1)
						{
							final Song song=itemInfoList.get(getAdapterPosition()).song;
							final File f=new File(song.path);
							AlertDialog.Builder ab=new AlertDialog.Builder(context);
							ab.setMessage(String.format(context.getString(R.string.info_detail),
							song.path,song.title,song.artist,song.album,
							MusicUtils.formatSongDuration(song.duration),song.duration,
							MusicUtils.formatDate(song.date_last_modified),
							MusicUtils.formatFileSize(f.length()),f.length()))
								.setPositiveButton(android.R.string.copy, new DialogInterface.OnClickListener(){

									@Override
									public void onClick(DialogInterface p1, int p2)
									{
										
									}
								}).setNegativeButton(android.R.string.cancel,null)
								.setTitle(R.string.detail)
								.setNeutralButton(R.string.view_file, new DialogInterface.OnClickListener(){
													  @Override
													  public void onClick(DialogInterface p1, int p2)
													  {
														  context.startActivity(new Intent().setDataAndType(Uri.fromFile(f),"audio/*"));
													  }
												  })
							.show();
						}
					});

				itemView.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View view)
						{
							TransitionManager.beginDelayedTransition(rvMusic, transition);	
							animator.setAnimateMoves(false);
							if (!itemView.isActivated())
							{
								itemView.setActivated(true);
								expandItem(MusicViewHolder.this, true);
							}
							else
							{
								itemView.setActivated(false);
								expandItem(MusicViewHolder.this, false);
							}
						}

					});

			}
			else
			{   ivChevron = (ImageView) ((ViewGroup)item).getChildAt(0);
				tvHeader = (TextView) ((ViewGroup)item).getChildAt(1);
				itemView.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v)
						{
							int i=viewHolder.getAdapterPosition();
							if (viewHolder.ivChevron.getRotation() == 0)
							{
								itemInfoList.get(i).collapz = true;
								viewHolder.ivChevron.animate().rotation(-90).start();
								for (i = i + 1;i < itemInfoList.size() && itemInfoList.get(i).type == ItemInfo.TYPE_ITEM_SONG;i++)
								{
									itemInfoList.get(i).collapz = true;
									notifyItemChanged(i);
								}
							}
							else
							{
								itemInfoList.get(i).collapz = false;
								viewHolder.ivChevron.animate().rotation(0).start();
								itemInfoList.get(viewHolder.getAdapterPosition()).collapz = false;
								for (i = i + 1;i < itemInfoList.size() && itemInfoList.get(i).type == ItemInfo.TYPE_ITEM_SONG;i++)
								{
									itemInfoList.get(i).collapz = false;
									notifyItemChanged(i);
								}

							}
						}
					});
			}
		}
	}


	public static class SimultaneousAnimator extends DefaultItemAnimator
	{

		private boolean animateMoves = true;

		public SimultaneousAnimator()
		{

            super();
        }

        void setAnimateMoves(boolean animateMoves)
		{
            this.animateMoves = animateMoves;
        }

        @Override
        public boolean animateMove(
			RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY)
		{
            if (!animateMoves)
			{
                dispatchMoveFinished(holder);
                return false;
            }
            return super.animateMove(holder, fromX, fromY, toX, toY);
        }
    }
}
