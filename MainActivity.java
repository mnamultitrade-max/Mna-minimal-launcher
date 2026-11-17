package com.mna.minimallauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private final int BG = Color.rgb(13, 14, 16);
    private final int PANEL = Color.rgb(27, 29, 33);
    private final int WHITE = Color.rgb(241, 243, 245);
    private final int MUTED = Color.rgb(151, 157, 166);
    private PackageManager pm;
    private final List<AppEntry> allApps = new ArrayList<>();
    private final List<AppEntry> shownApps = new ArrayList<>();
    private LinearLayout root, content;
    private EditText search;
    private GridView grid;
    private TextView heading;
    private boolean drawerOpen = false;
    private AppAdapter adapter;

    static class AppEntry {
        String label, packageName;
        Drawable icon;
        AppEntry(String l, String p, Drawable i) { label=l; packageName=p; icon=i; }
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        pm = getPackageManager();
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(0);
        loadApps();
        showHome();
    }

    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }
    private TextView text(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }
    private LinearLayout vertical() {
        LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l;
    }
    private LinearLayout horizontal() {
        LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL); return l;
    }
    private void pad(View v, int l, int t, int r, int b) { v.setPadding(dp(l),dp(t),dp(r),dp(b)); }

    private void loadApps() {
        allApps.clear();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<android.content.pm.ResolveInfo> infos = pm.queryIntentActivities(intent, 0);
        for (android.content.pm.ResolveInfo ri : infos) {
            String pkg = ri.activityInfo.packageName;
            if (pkg.equals(getPackageName())) continue;
            String label = ri.loadLabel(pm).toString();
            Drawable icon = ri.loadIcon(pm);
            boolean exists = false;
            for (AppEntry a : allApps) if (a.packageName.equals(pkg)) { exists=true; break; }
            if (!exists) allApps.add(new AppEntry(label, pkg, icon));
        }
        Collections.sort(allApps, Comparator.comparing(a -> a.label.toLowerCase(Locale.ROOT)));
    }

    private void base() {
        root = vertical(); root.setBackgroundColor(BG);
        setContentView(root);
        content = vertical();
        root.addView(content, new LinearLayout.LayoutParams(-1,-1));
    }

    private void showHome() {
        drawerOpen = false; base();
        LinearLayout top = vertical(); pad(top,24,42,24,12);
        TextView clock = text(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()), 54, WHITE, true);
        TextView date = text(new SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(new Date()), 15, MUTED, false);
        top.addView(clock); top.addView(date);
        content.addView(top);

        EditText homeSearch = new EditText(this);
        homeSearch.setSingleLine(true); homeSearch.setTextColor(WHITE); homeSearch.setHintTextColor(MUTED);
        homeSearch.setHint("Search apps"); homeSearch.setTextSize(15);
        homeSearch.setCompoundDrawablePadding(dp(10));
        homeSearch.setBackground(round(PANEL,18)); pad(homeSearch,16,4,16,4);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1,dp(52));
        sp.setMargins(dp(20),dp(22),dp(20),dp(18)); content.addView(homeSearch,sp);
        homeSearch.setOnFocusChangeListener((v,has)-> { if(has) { showDrawer(); search.setText(homeSearch.getText()); search.requestFocus(); } });
        homeSearch.setOnClickListener(v->{ showDrawer(); search.setText(homeSearch.getText()); search.requestFocus(); });

        TextView favTitle = text("FAVOURITES", 11, MUTED, true); pad(favTitle,24,10,24,8); content.addView(favTitle);
        LinearLayout fav = horizontal(); fav.setGravity(Gravity.CENTER);
        int count = Math.min(5, allApps.size());
        for(int i=0;i<count;i++) fav.addView(appTile(allApps.get(i), true), new LinearLayout.LayoutParams(0,dp(92),1));
        content.addView(fav);

        View spacer = new View(this); content.addView(spacer,new LinearLayout.LayoutParams(1,0,1));
        TextView open = text("☷     ALL APPS",14,WHITE,true);
        open.setGravity(Gravity.CENTER); open.setBackground(round(PANEL,24));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1,dp(54));
        bp.setMargins(dp(20),dp(8),dp(20),dp(26)); content.addView(open,bp);
        open.setOnClickListener(v->showDrawer());

        TextView hint = text("MNA MINIMAL  •  Long-press an app for App Info",10,MUTED,false);
        hint.setGravity(Gravity.CENTER); pad(hint,0,0,0,12); content.addView(hint);
    }

    private android.graphics.drawable.GradientDrawable round(int color, int radius) {
        android.graphics.drawable.GradientDrawable d = new android.graphics.drawable.GradientDrawable();
        d.setColor(color); d.setCornerRadius(dp(radius)); return d;
    }

    private View appTile(AppEntry app, boolean compact) {
        LinearLayout tile = vertical(); tile.setGravity(Gravity.CENTER); tile.setPadding(dp(3),dp(7),dp(3),dp(7));
        ImageView icon = new ImageView(this); icon.setImageDrawable(app.icon);
        int sz = compact ? 42 : 44;
        tile.addView(icon,new LinearLayout.LayoutParams(dp(sz),dp(sz)));
        TextView name = text(app.label, compact?10:11, WHITE, false);
        name.setGravity(Gravity.CENTER); name.setMaxLines(1); name.setEllipsize(android.text.TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(-1,dp(22)); np.topMargin=dp(5); tile.addView(name,np);
        tile.setOnClickListener(v->launch(app));
        tile.setOnLongClickListener(v->{ appInfo(app); return true; });
        return tile;
    }

    private void showDrawer() {
        drawerOpen = true; base();
        LinearLayout bar = horizontal(); pad(bar,20,22,20,10);
        TextView back = text("‹",34,WHITE,true); bar.addView(back,new LinearLayout.LayoutParams(dp(42),dp(48)));
        back.setOnClickListener(v->showHome());
        heading = text("All apps",22,WHITE,true); bar.addView(heading,new LinearLayout.LayoutParams(0,-2,1));
        TextView total = text(String.valueOf(allApps.size()),12,MUTED,false); bar.addView(total);
        content.addView(bar);

        search = new EditText(this); search.setSingleLine(true); search.setTextColor(WHITE);
        search.setHintTextColor(MUTED); search.setHint("Search apps"); search.setTextSize(15);
        search.setBackground(round(PANEL,16)); pad(search,16,2,16,2);
        LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(-1,dp(50));
        ep.setMargins(dp(20),dp(8),dp(20),dp(12)); content.addView(search,ep);

        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true);
        grid = new GridView(this); grid.setNumColumns(4); grid.setVerticalSpacing(dp(8));
        grid.setHorizontalSpacing(dp(2)); grid.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        grid.setSelector(android.R.color.transparent);
        adapter = new AppAdapter(); grid.setAdapter(adapter);
        scroll.addView(grid); content.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));

        TextView footer = text("A–Z  •  Long-press for App Info",10,MUTED,false);
        footer.setGravity(Gravity.CENTER); pad(footer,0,10,0,16); content.addView(footer);
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            public void onTextChanged(CharSequence s,int st,int before,int count){ filter(s.toString()); }
            public void afterTextChanged(Editable e){}
        });
    }

    private void filter(String q) {
        shownApps.clear();
        String query = q.toLowerCase(Locale.ROOT).trim();
        for(AppEntry a:allApps) if(a.label.toLowerCase(Locale.ROOT).contains(query)) shownApps.add(a);
        if(adapter!=null) adapter.notifyDataSetChanged();
        if(heading!=null) heading.setText(query.isEmpty()?"All apps":"Search results");
    }

    private class AppAdapter extends BaseAdapter {
        public int getCount(){return shownApps.size();}
        public Object getItem(int p){return shownApps.get(p);}
        public long getItemId(int p){return p;}
        public View getView(int p,View convert,ViewGroup parent){return appTile(shownApps.get(p),false);}
    }

    private void launch(AppEntry app) {
        Intent i = pm.getLaunchIntentForPackage(app.packageName);
        if(i!=null) { i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(i); }
    }
    private void appInfo(AppEntry app) {
        new AlertDialog.Builder(this).setTitle(app.label)
            .setItems(new String[]{"Open app","App info"},(d,w)->{
                if(w==0) launch(app);
                else { Intent i=new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:"+app.packageName)); startActivity(i); }
            }).setNegativeButton("Cancel",null).show();
    }

    @Override public void onBackPressed() {
        if(drawerOpen) showHome(); else super.onBackPressed();
    }
    @Override protected void onResume() {
        super.onResume();
        if(pm!=null) {
            loadApps();
            if(drawerOpen && adapter!=null) { filter(search==null?"":search.getText().toString()); }
        }
    }
}
