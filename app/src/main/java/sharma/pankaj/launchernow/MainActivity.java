package sharma.pankaj.launchernow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import sharma.pankaj.launchernow.adapter.HomeAdapter;
import sharma.pankaj.launchernow.model.HomeModel;

public class MainActivity extends AppCompatActivity {

    List<HomeModel> homeodel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PackageManager pm = getPackageManager();
        homeodel = new ArrayList<>();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        @SuppressLint("QueryPermissionsNeeded")
        List<ResolveInfo> allApps = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : allApps) {
            homeodel.add(new HomeModel(resolveInfo.loadLabel(pm),
                    resolveInfo.activityInfo.packageName,
                    resolveInfo.activityInfo.loadIcon(pm)));
        }
        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        recyclerView.setPadding(16,16,16,16);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 5));
        recyclerView.setAdapter(new HomeAdapter(this, homeodel));
        setContentView(recyclerView);
    }
}