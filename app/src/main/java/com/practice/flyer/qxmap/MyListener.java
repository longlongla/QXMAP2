package com.practice.flyer.qxmap;




import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class MyListener implements OnClickListener{
    int mPosition;
    public MyListener(int inPosition){
        mPosition= inPosition;
    }
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
    	System.out.println("ss1ss");
        //Toast.makeText(ListViewActivity.this, title[mPosition], Toast.LENGTH_SHORT).show();
    }

}
