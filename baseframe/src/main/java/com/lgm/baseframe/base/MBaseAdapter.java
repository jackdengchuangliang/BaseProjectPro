package com.lgm.baseframe.base;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.LinkedList;
import java.util.List;


/**
 * 适配器基类
 *
 *
 * @param <T>
 */
public abstract class MBaseAdapter<T> extends BaseAdapter {
	
	private final List<T> mList = new LinkedList<T>();
	
	public List<T> getList(){
		return mList;
	}
	
	public void appendToList(List<T> list) {
		if (list == null) {
			return;
		}
		mList.addAll(list);
		notifyDataSetChanged();
	}
	public void clearList(){
		mList.clear();
	}
	public void appendToTopList(List<T> list) {
		if (list == null) {
			return;
		}
		mList.addAll(0, list);
		notifyDataSetChanged();
	}

	public void clear() {
		mList.clear();
		notifyDataSetChanged();
	}


	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		if(position >(mList.size()-1)){
			return null;
		}
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(position == mList.size()- 1){
			onReachBottom();
		}
		return getExView(position,convertView,parent);
	}
	protected abstract View getExView(int position, View convertView, ViewGroup parent);

	protected void onReachBottom(){}

}
