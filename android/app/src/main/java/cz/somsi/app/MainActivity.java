package cz.somsi.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.*;

public class MainActivity extends Activity {
    private static final String WEB = "https://stavbysomsi.cz/";
    private static final String PHONE = "+420736771754";
    private static final int BG = Color.rgb(4, 10, 14);
    private static final int GOLD = Color.rgb(245, 178, 43);
    private static final int PANEL = Color.rgb(16, 25, 31);
    private FrameLayout root;
    private float sx, sy;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        hideSystemBars();
        showHome();
    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemBars();
    }

    private void hideSystemBars() {
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController c = getWindow().getInsetsController();
            if (c != null) {
                c.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                c.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    private void showHome() {
        root = new FrameLayout(this);
        root.setBackgroundColor(BG);
        ImageView reference = new ImageView(this);
        reference.setImageResource(R.drawable.somsi_home_reference);
        reference.setScaleType(ImageView.ScaleType.FIT_XY);
        root.addView(reference, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
        root.post(() -> {
            sx = root.getWidth() / 759f;
            sy = root.getHeight() / 1511f;
            addHomeHotspots();
        });
    }

    private View hotspot(float x, float y, float w, float h, final Runnable action) {
        View v = new View(this);
        v.setBackgroundColor(Color.TRANSPARENT);
        v.setOnClickListener(view -> action.run());
        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(Math.max(1, Math.round(w * sx)), Math.max(1, Math.round(h * sy)));
        p.leftMargin = Math.round(x * sx);
        p.topMargin = Math.round(y * sy);
        root.addView(v, p);
        return v;
    }

    private void addHomeHotspots() {
        hotspot(0,72,105,100,this::showMenu);
        hotspot(670,70,88,105,this::showNotifications);
        hotspot(4,716,242,112,() -> openSection("Zakázky"));
        hotspot(252,716,247,112,this::openCamera);
        hotspot(505,716,250,112,() -> openSection("Stavební deník"));
        hotspot(4,832,242,112,() -> openSection("Úkoly"));
        hotspot(252,832,247,112,() -> openSection("Docházka"));
        hotspot(505,832,250,112,() -> openSection("Materiál"));
        hotspot(4,944,242,112,() -> openSection("Dokumenty"));
        hotspot(252,944,247,112,() -> openSection("Adresář"));
        hotspot(505,944,250,112,this::showChat);
        hotspot(455,1075,285,102,this::callPhone);
        hotspot(4,1070,440,108,this::callPhone);
        hotspot(0,1362,150,149,this::showHome);
        hotspot(150,1362,150,149,() -> openSection("Zakázky"));
        hotspot(300,1340,160,171,this::newRecord);
        hotspot(450,1362,150,149,this::showChat);
        hotspot(600,1362,159,149,this::showMenu);
    }

    private void showNotifications() {
        new AlertDialog.Builder(this).setTitle("Oznámení SOMSI")
            .setMessage("3 nové události\n\n• Nová zpráva v chatu\n• Aktualizace zakázky\n• Nový záznam v dokumentech")
            .setPositiveButton("OK",null).show();
    }

    private void showMenu() {
        final String[] items={"🌐  Webové stránky SOMSI","📞  Zavolat","💬  Chat","📷  Fotodokumentace","📋  Zakázky","👤  Adresář","ℹ️  O aplikaci"};
        new AlertDialog.Builder(this).setTitle("SOMSI stavební práce").setItems(items,(d,which)->{
            switch(which){case 0:openWeb();break;case 1:callPhone();break;case 2:showChat();break;case 3:openCamera();break;case 4:openSection("Zakázky");break;case 5:openSection("Adresář");break;default:showAbout();}
        }).setNegativeButton("Zavřít",null).show();
    }

    private void showAbout(){new AlertDialog.Builder(this).setTitle("SOMSI stavební práce").setMessage("Od základu po střechu\n\nAplikace SOMSI pro zakázky, fotodokumentaci, stavební deník, úkoly, docházku, materiál, dokumenty, adresář a komunikaci.").setPositiveButton("OK",null).show();}
    private void callPhone(){try{startActivity(new Intent(Intent.ACTION_DIAL,Uri.parse("tel:"+PHONE)));}catch(Exception e){Toast.makeText(this,"Telefon není dostupný",Toast.LENGTH_SHORT).show();}}
    private void openWeb(){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(WEB)));}catch(Exception e){Toast.makeText(this,"Web nelze otevřít",Toast.LENGTH_SHORT).show();}}
    private void openCamera(){try{startActivity(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));}catch(Exception e){Toast.makeText(this,"Fotoaparát není dostupný",Toast.LENGTH_SHORT).show();}}

    private TextView text(String s,int sp,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(sp);t.setTextColor(color);t.setTypeface(android.graphics.Typeface.DEFAULT,bold?1:0);t.setPadding(0,4,0,4);return t;}

    private void openSection(String title){
        LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setBackgroundColor(BG);page.setPadding(22,22,22,22);
        LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);
        TextView back=text("‹",38,GOLD,true);back.setGravity(Gravity.CENTER);back.setOnClickListener(v->showHome());header.addView(back,new LinearLayout.LayoutParams(58,62));
        header.addView(text(title,24,Color.WHITE,true),new LinearLayout.LayoutParams(0,62,1));page.addView(header);
        ScrollView scroll=new ScrollView(this);LinearLayout list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);
        String[] lines;
        switch(title){case "Zakázky":lines=new String[]{"Aktivní zakázky • 86","Rekonstrukce domu – Třemošnice","Hrubá stavba – Pardubice","Fasáda – Vysočina","Plánované realizace – 12"};break;case "Stavební deník":lines=new String[]{"Dnešní záznam","Postup prací","Fotografie","Poznámky stavby","Přidat nový záznam"};break;case "Úkoly":lines=new String[]{"Dnešní úkoly","Kontrola materiálu","Dokončit omítky","Objednat okna","Kontrola předání"};break;case "Docházka":lines=new String[]{"Dnešní docházka","Přihlásit příchod","Přihlásit odchod","Historie docházky"};break;case "Materiál":lines=new String[]{"Sklad a materiál","Cement • 48 ks","Izolace • 24 balení","Dlažba • 36 m²","Přidat materiál"};break;case "Dokumenty":lines=new String[]{"Dokumenty zakázek","Smlouvy","Rozpočty","Předávací protokoly","Faktury"};break;case "Adresář":lines=new String[]{"Kontakty","SOMSI – kancelář","Stavbyvedoucí","Dodavatelé","Klienti"};break;default:lines=new String[]{"Přehled","Nový záznam","Historie","Nastavení"};}
        for(String line:lines){Button b=new Button(this);b.setText(line);b.setTextColor(Color.WHITE);b.setTextSize(16);b.setAllCaps(false);b.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);b.setBackgroundColor(PANEL);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,64);p.setMargins(0,7,0,7);list.addView(b,p);}
        scroll.addView(list);page.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));setContentView(page);
    }

    private void showChat(){
        LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setBackgroundColor(BG);page.setPadding(18,18,18,12);
        LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);TextView back=text("‹",38,GOLD,true);back.setOnClickListener(v->showHome());header.addView(back,new LinearLayout.LayoutParams(58,62));header.addView(text("Chat SOMSI",24,Color.WHITE,true),new LinearLayout.LayoutParams(0,62,1));page.addView(header);
        TextView messages=text("Petr • 09:12\nZakázka Třemošnice – práce pokračují podle plánu.\n\nVy • 09:18\nDěkuji, potvrzuji.\n\nJana • 09:26\nNové fotografie jsou nahrané.",16,Color.WHITE,false);messages.setBackgroundColor(PANEL);messages.setPadding(18,18,18,18);page.addView(messages,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout compose=new LinearLayout(this);EditText input=new EditText(this);input.setHint("Napište zprávu…");input.setTextColor(Color.WHITE);input.setHintTextColor(Color.GRAY);compose.addView(input,new LinearLayout.LayoutParams(0,60,1));Button send=new Button(this);send.setText("Odeslat");send.setTextColor(Color.BLACK);send.setBackgroundColor(GOLD);send.setOnClickListener(v->{if(input.getText().length()>0){messages.append("\n\nVy • nyní\n"+input.getText());input.setText("");}});compose.addView(send,new LinearLayout.LayoutParams(110,60));page.addView(compose);setContentView(page);
    }

    private void newRecord(){final String[] items={"Nová zakázka","Nová fotografie","Nový záznam deníku","Nový úkol","Nový materiál","Nový dokument"};new AlertDialog.Builder(this).setTitle("Nový záznam").setItems(items,(d,which)->{if(which==1)openCamera();else Toast.makeText(this,items[which]+" – připraveno",Toast.LENGTH_SHORT).show();}).setNegativeButton("Zrušit",null).show();}
}
