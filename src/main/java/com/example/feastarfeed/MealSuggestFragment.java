package com.example.feastarfeed;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bluehomestudio.luckywheel.LuckyWheel;
import com.bluehomestudio.luckywheel.OnLuckyWheelReachTheTarget;
import com.bluehomestudio.luckywheel.WheelItem;

import java.util.List;
import java.util.Random;

public class MealSuggestFragment extends Fragment {

    private LuckyWheel luckyWheel;

    List<WheelItem> wheelItemList;

    TextView start;

    String points, points_amount;

    int count=0;

    ImageView close;

    private OnCountChangeListenerPop onCountChangeListenerpop;

    public MealSuggestFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_meal_suggest, container, false);

        wheelItemList = AccountFragment.wheelItems;
        luckyWheel = view.findViewById(R.id.lwv);
        luckyWheel.addWheelItems(wheelItemList);

        luckyWheel.setLuckyWheelReachTheTarget(new OnLuckyWheelReachTheTarget() {
            @Override
            public void onReachTarget() {
                WheelItem itemSelected = wheelItemList.get(Integer.parseInt(points)-1);
                points_amount = itemSelected.text;
                showPopup();
            }
        });

        start = view.findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Random random = new Random();
                points = String.valueOf(random.nextInt(AccountFragment.count));
                if (points.equals("0"))
                {
                    points = String.valueOf(1);
                }

                luckyWheel.rotateWheelTo(Integer.parseInt(points));
            }
        });

        if (getActivity() != null && getActivity() instanceof OnCountChangeListenerPop) {
            setOnCountChangeListener((OnCountChangeListenerPop) getActivity());
        } else {
            throw new IllegalStateException("Activity must implement OnCountChangeListener");
        }

        close = view.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_in2, R.anim.slide_out2);
                fragmentTransaction.replace(R.id.frame_layout,new AccountFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        return view;
    }

    public void showPopup(){
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.result_popup);
        dialog.setCancelable(true);
        dialog.show();

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);

        TextView winText = dialog.findViewById(R.id.win_text);
        winText.setText(points_amount);
        winText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onCountChangeListenerpop != null) {
                    onCountChangeListenerpop.onCountChangedPop(count,points_amount);
                }
                Log.d("tagFragment","text = "+points_amount);
                Log.d("tagFragment","count = "+count);
                dialog.cancel();
            }
        });

        TextView btn = dialog.findViewById(R.id.button);
        btn.setOnClickListener(view -> {
            dialog.cancel();

        });

        // Check if github push was successful

    }

    public interface OnCountChangeListenerPop {
        void onCountChangedPop(int count,String text);

    }

    public void setOnCountChangeListener(OnCountChangeListenerPop listener) {
        this.onCountChangeListenerpop = listener;
    }

}