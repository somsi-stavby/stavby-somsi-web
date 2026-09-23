package cz.somsi.app.v7;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    static final String WEB = "https://stavbysomsi.cz/";
    static final String PHONE = "+420736771754";
    static final String EMAIL = "petrsomsi@seznam.cz";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        immersive();
        setContentView(new HomeView());
    }

    private void immersive() {
        Window w = getWindow();
        w.setStatusBarColor(Color.BLACK);
        w.setNavigationBarColor(Color.BLACK);
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController c = w.getInsetsController();
            if (c != null) {
                c.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                c.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            w.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    private void openUrl(String url) {
        try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
        catch (Exception e) { Toast.makeText(this, "Odkaz se nepodařilo otevřít", Toast.LENGTH_SHORT).show(); }
    }
    private void call() {
        try { startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + PHONE))); }
        catch (Exception e) { Toast.makeText(this, PHONE, Toast.LENGTH_LONG).show(); }
    }
    private void mail() {
        try {
            Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + EMAIL));
            i.putExtra(Intent.EXTRA_SUBJECT, "Kontakt ze SOMSI aplikace");
            startActivity(i);
        } catch (Exception e) { Toast.makeText(this, EMAIL, Toast.LENGTH_LONG).show(); }
    }
    private void maps() {
        try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("geo:49.886,15.819?q=Horní+Studenec+29"))); }
        catch (Exception e) { openUrl("https://www.google.com/maps/search/?api=1&query=Horní+Studenec+29"); }
    }

    private class HomeView extends View {
        Bitmap bg;
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        RectF dst = new RectF();
        final float BW = 764f, BH = 1510f;
        HomeView() {
            super(MainActivity.this);
            bg = BitmapFactory.decodeResource(getResources(), R.drawable.somsi_home);
            setBackgroundColor(Color.BLACK);
        }
        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            float s = Math.min(getWidth()/BW, getHeight()/BH);
            float w = BW*s, h = BH*s;
            float l=(getWidth()-w)/2f, t=(getHeight()-h)/2f;
            dst.set(l,t,l+w,t+h);
            c.drawBitmap(bg,null,dst,p);
        }
        boolean hit(float x,float y,float l,float t,float r,float b){
            float s=Math.min(getWidth()/BW,getHeight()/BH);
            float ox=(getWidth()-BW*s)/2f, oy=(getHeight()-BH*s)/2f;
            return x>=ox+l*s && x<=ox+r*s && y>=oy+t*s && y<=oy+b*s;
        }
        @Override public boolean onTouchEvent(MotionEvent e) {
            if(e.getAction()!=MotionEvent.ACTION_UP) return true;
            float x=e.getX(), y=e.getY();
            if(hit(x,y,5,70,95,150)) { showMenu(); return true; }
            if(hit(x,y,650,55,760,155)) { showNotifications(); return true; }
            if(hit(x,y,220,65,570,300)) { openUrl(WEB); return true; }
            if(hit(x,y,5,715,250,830)) { section("Zakázky", "Přehled aktivních, dokončených a plánovaných zakázek.", new String[]{"Aktivní zakázky: 86","Dokončené zakázky: 67","Plánované zakázky: 12"}); return true; }
            if(hit(x,y,255,715,510,830)) { section("Fotodokumentace", "Fotografie realizací a dokumentace staveb.", new String[]{"Realizace 1–6","Hlavní realizace","Připraveno pro další fotografie"}); return true; }
            if(hit(x,y,515,715,760,830)) { section("Stavební deník", "Denní záznamy, poznámky a průběh prací.", new String[]{"Dnešní záznam","Historie záznamů","Poznámky k zakázkám"}); return true; }
            if(hit(x,y,5,835,250,945)) { section("Úkoly", "Přehled úkolů a termínů.", new String[]{"Prioritní úkoly","Termíny","Dokončené úkoly"}); return true; }
            if(hit(x,y,255,835,510,945)) { section("Docházka", "Evidence pracovní docházky.", new String[]{"Dnes","Tento týden","Přehled pracovníků"}); return true; }
            if(hit(x,y,515,835,760,945)) { section("Materiál", "Evidence materiálu a spotřeby.", new String[]{"Sklad","Objednávky","Spotřeba na zakázkách"}); return true; }
            if(hit(x,y,5,950,250,1060)) { section("Dokumenty", "Dokumenty k zakázkám a firmě.", new String[]{"Smlouvy","Rozpočty","Dokumentace"}); return true; }
            if(hit(x,y,255,950,510,1060)) { section("Adresář", "Kontakty na firmu, pracovníky a dodavatele.", new String[]{"SOMSI stavební práce","Petr SOMSI","Dodavatelé a kontakty"}); return true; }
            if(hit(x,y,515,950,760,1060)) { chat(); return true; }
            if(hit(x,y,5,1070,760,1180)) { call(); return true; }
            if(hit(x,y,5,1190,390,1355)) { maps(); return true; }
            if(hit(x,y,390,1190,760,1355)) { openUrl(WEB); return true; }
            if(hit(x,y,0,1360,150,1505)) { return true; }
            if(hit(x,y,150,1360,300,1505)) { section("Zakázky", "Přehled zakázek", new String[]{"Aktivní: 86","Dokončené: 67","Plánované: 12"}); return true; }
            if(hit(x,y,300,1360,455,1505)) { section("Nový záznam", "Rychlý nový záznam", new String[]{"Nová zakázka","Nový úkol","Nový zápis do deníku"}); return true; }
            if(hit(x,y,455,1360,610,1505)) { chat(); return true; }
            if(hit(x,y,610,1360,764,1505)) { showMenu(); return true; }
            return true;
        }
    }

    private void showNotifications(){
        new android.app.AlertDialog.Builder(this)
            .setTitle("Oznámení SOMSI")
            .setItems(new String[]{"3 nové zprávy v chatu","Aktivní zakázky: 86","Plánované zakázky: 12"},null)
            .setPositiveButton("Zavřít",null).show();
    }

    private void showMenu(){
        final String[] items={"Domů","Zakázky","Fotodokumentace","Stavební deník","Úkoly","Docházka","Materiál","Dokumenty","Adresář","Chat","Web stavbysomsi.cz","Zavolat SOMSI","Napsat e-mail"};
        new android.app.AlertDialog.Builder(this).setTitle("SOMSI").setItems(items,(dlg,which)->{
            if(which==10) openUrl(WEB); else if(which==11) call(); else if(which==12) mail();
            else if(which==9) chat(); else if(which==0) { }
            else section(items[which], "Sekce aplikace SOMSI", new String[]{"Obsah sekce je připraven pro další napojení na databázi.","Synchronizace zakázek","Dokumentace a historie"});
        }).setNegativeButton("Zavřít",null).show();
    }

    private void chat(){
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(28,30,28,24); root.setBackgroundColor(Color.rgb(17,24,32));
        root.addView(t("SOMSI Chat",26,Color.WHITE));
        root.addView(t("Kontaktujte SOMSI. Můžete pokračovat přes WhatsApp nebo e-mail.",16,Color.LTGRAY));
        Button wa=new Button(this); wa.setText("Otevřít WhatsApp"); wa.setOnClickListener(v->openUrl("https://wa.me/420736771754")); root.addView(wa);
        Button em=new Button(this); em.setText("Napsat e-mail"); em.setOnClickListener(v->mail()); root.addView(em);
        Button ph=new Button(this); ph.setText("Zavolat +420 736 771 754"); ph.setOnClickListener(v->call()); root.addView(ph);
        Button close=new Button(this); close.setText("Zpět na úvod"); close.setOnClickListener(v->setContentView(new HomeView())); root.addView(close);
        setContentView(root);
    }

    private TextView t(String s,int size,int color){ TextView v=new TextView(this); v.setText(s); v.setTextSize(size); v.setTextColor(color); v.setPadding(0,0,0,20); return v; }

    private void section(String title,String subtitle,String[] rows){
        LinearLayout body=new LinearLayout(this); body.setOrientation(LinearLayout.VERTICAL); body.setPadding(24,24,24,24); body.setBackgroundColor(Color.rgb(17,24,32));
        body.addView(t("SOMSI  •  "+title,26,Color.WHITE));
        body.addView(t(subtitle,15,Color.LTGRAY));
        for(String row:rows){ TextView v=t("▣  "+row,17,Color.WHITE); v.setBackgroundColor(Color.rgb(27,38,48)); v.setPadding(18,18,18,18); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.setMargins(0,0,0,12); body.addView(v,lp); }
        Button web=new Button(this); web.setText("Otevřít web SOMSI"); web.setOnClickListener(v->openUrl(WEB)); body.addView(web);
        Button back=new Button(this); back.setText("← Zpět na úvod"); back.setOnClickListener(v->setContentView(new HomeView())); body.addView(back);
        ScrollView sv=new ScrollView(this); sv.addView(body); setContentView(sv);
    }
}
