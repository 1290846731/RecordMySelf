package com.miaojun.record;

import android.view.View;

/**
 * Created by miaojun on 16/11/3.
 */

public interface OnItemClickListener {
    void onItemClick(View v, int position);
    void onLongClick(View v, int position);
}
