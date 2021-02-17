package sharma.pankaj.launchernow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import sharma.pankaj.launchernow.adapter.HomeAdapter;
import sharma.pankaj.launchernow.model.HomeModel;

public class MainActivity extends AppCompatActivity {

    private List<HomeModel> homeModels;
    private HomeAdapter adapter;
    private RecyclerView recyclerView;
    private EditText editText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout main = new LinearLayout(this);
        editText = new EditText(this);
        editText.setTextColor(getResources().getColor(R.color.white));
        main.setOrientation(LinearLayout.VERTICAL);
        editText.setPadding(16, 8, 16, 8);
        editText.setMaxLines(1);
        editText.setHint("Search...");
        editText.setHintTextColor(getResources().getColor(R.color.gray));
        editText.setBackgroundColor(getResources().getColor(R.color.black));
        ((EditText)editText).setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
        main.addView(editText,ViewGroup.LayoutParams.MATCH_PARENT, 60);


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

        recyclerView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        recyclerView.setPadding(16,16,16,16);
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
    private void setHomeAdapter(List<HomeModel> list){
        adapter = new HomeAdapter(this, homeModels);
        recyclerView.setAdapter(adapter);
    }

    private void filter(String key){
        List<HomeModel> filteredList = new ArrayList<>();
        for (HomeModel item : homeModels) {
            if (item.getName().toString().toLowerCase().contains(key.toLowerCase())) {
                filteredList.add(item);
            }
        }
        adapter.filterList(filteredList);
    }
}