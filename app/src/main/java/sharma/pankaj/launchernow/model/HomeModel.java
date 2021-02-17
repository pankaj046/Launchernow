package sharma.pankaj.launchernow.model;

import android.graphics.drawable.Drawable;

public class HomeModel {

    private CharSequence name;
    private CharSequence packageName;
    private Drawable icon;

    public HomeModel(CharSequence name, CharSequence packageName, Drawable icon) {
        this.name = name;
        this.packageName = packageName;
        this.icon = icon;
    }

    public CharSequence getName() {
        return name;
    }

    public void setName(CharSequence name) {
        this.name = name;
    }

    public CharSequence getPackageName() {
        return packageName;
    }

    public void setPackageName(CharSequence packageName) {
        this.packageName = packageName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    @Override
    public String toString() {
        return "HomeModel{" +
                "name=" + name +
                '}';
    }
}
