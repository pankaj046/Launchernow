package sharma.pankaj.launchernow;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import sharma.pankaj.launchernow.adapter.HomeAdapter;
import sharma.pankaj.launchernow.listener.ItemClickListener;
import sharma.pankaj.launchernow.model.HomeModel;
import static sharma.pankaj.launchernow.util.Constants.CODE;

public class MainActivity extends AppCompatActivity {

    private List<HomeModel> homeModels;
    private HomeAdapter adapter;
    private RecyclerView recyclerView;
    private EditText editText;
    int position = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout main = new LinearLayout(this);
        editText = new EditText(this);
        editText.setTextColor(getResources().getColor(R.color.white));
        main.setOrientation(LinearLayout.VERTICAL);
        editText.setPadding(16, 8, 16, 8);
        editText.setMaxLines(1);
        editText.setSingleLine(true);
        editText.setHint("Search...");
        editText.setHintTextColor(getResources().getColor(R.color.gray));
        editText.setBackgroundColor(getResources().getColor(R.color.black));
        ((EditText) editText).setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
        main.addView(editText, ViewGroup.LayoutParams.MATCH_PARENT, 60);


        PackageManager pm = getPackageManager();
        homeModels = new ArrayList<>();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        @SuppressLint("QueryPermissionsNeeded")
        List<ResolveInfo> allApps = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : allApps) {
            homeModels.add(new HomeModel(resolveInfo.loadLabel(pm),
                    resolveInfo.activityInfo.packageName,
                    resolveInfo.activityInfo.loadIcon(pm)));
        }
        recyclerView = new RecyclerView(this);
        recyclerView.setItemViewCacheSize(1);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        recyclerView.setPadding(16, 16, 16, 16);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 5));
        main.addView(recyclerView);
        setHomeAdapter(homeModels);


        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        setContentView(main);
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    private void setHomeAdapter(List<HomeModel> list) {
        adapter = new HomeAdapter(this, list, new ItemClickListener() {
            @Override
            public void onItemClick(String requestType, String value, int position) {
                try {
                    setPosition(position);
                    Intent intent=new Intent(Intent.ACTION_DELETE);
                    intent.setData(Uri.parse("package:" + value));
                    startActivityForResult(intent, CODE);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        AnimationSet set = new AnimationSet(true);

        // Fade in animation
        Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(400);
        fadeIn.setFillAfter(true);
        set.addAnimation(fadeIn);

        // Slide up animation from bottom of screen
        Animation slideUp = new TranslateAnimation(0, 0,  ViewGroup.LayoutParams.MATCH_PARENT, 0);
        slideUp.setInterpolator(new DecelerateInterpolator(4.f));
        slideUp.setDuration(400);
        set.addAnimation(slideUp);

        // Set up the animation controller              (second parameter is the delay)
        LayoutAnimationController controller = new LayoutAnimationController(set, 0.2f);
        recyclerView.setLayoutAnimation(controller);

        recyclerView.setAdapter(adapter);
        recyclerView.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        recyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                        for (int i = 0; i < recyclerView.getChildCount(); i++) {
                            View v = recyclerView.getChildAt(i);
                            v.setAlpha(0.0f);
                            v.animate().alpha(1.0f)
                                    .setDuration(300)
                                    .setStartDelay(i * 50)
                                    .start();
                        }
                        return true;
                    }
                });
    }

    private void filter(String key) {
        List<HomeModel> filteredList = new ArrayList<>();
        for (HomeModel item : homeModels) {
            if (item.getName().toString().toLowerCase().contains(key.toLowerCase())) {
                filteredList.add(item);
            }
        }
        adapter.filterList(filteredList);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE) {
            if (resultCode == RESULT_OK) {
                if (getPosition()!=-1){
                    homeModels.remove(getPosition());
                    if (adapter != null)
                        adapter.notifyItemChanged(position);
                } else {
                    Log.e("hjj", "onActivityResult: else");
                }
            } else if (resultCode == RESULT_CANCELED) {
                Log.d("TAG", "onActivityResult: user canceled the (un)install");
            } else if (resultCode == RESULT_FIRST_USER) {
                Log.d("TAG", "onActivityResult: failed to (un)install");
            }
        }

//        if (requestCode == ACTIVITY_RESULT){
//            if (requestCode == RESULT_OK){
//                Log.e("hjj", "onActivityResult: if" );
//                if (getPosition()!=-1 && getPosition()<homeModels.size()){
//                    homeModels.remove(getPosition());
//                    if (adapter!=null)
//                        adapter.notifyItemChanged(position);
//                }else {
//                    Log.e("hjj", "onActivityResult: else" );
//                }
//            }else {
//                Log.e("hjj", "onActivityResult: RESULT_OK" );
//            }
//        }
    }
}