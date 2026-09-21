package cz.somsi.app;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    final int GOLD=Color.rgb(232,169,46), DARK=Color.rgb(7,13,17), PANEL=Color.rgb(20,28,33), TEXT=Color.WHITE, MUTED=Color.rgb(190,195,198);
    FrameLayout root; SomsiView content; TextView title; SharedPreferences prefs;
    int visits, active, done, planned; String section="Domů";

    @Override public void onCreate(Bundle b){ super.onCreate(b); prefs=getSharedPreferences("somsi",0); load(); build(); }
    void load(){ visits=prefs.getInt("visits",1248); active=prefs.getInt("active",86); done=prefs.getInt("done",67); planned=prefs.getInt("planned",12); }
    void save(){ prefs.edit().putInt("visits",visits).putInt("active",active).putInt("done",done).putInt("planned",planned).apply(); }

    void build(){
        root=new FrameLayout(this); root.setBackgroundColor(DARK);
        content=new SomsiView(this); ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true); scroll.addView(content,new ScrollView.LayoutParams(-1,dp(1120))); root.addView(scroll,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout nav=new LinearLayout(this); nav.setOrientation(LinearLayout.HORIZONTAL); nav.setGravity(Gravity.CENTER); nav.setPadding(dp(8),dp(5),dp(8),dp(5)); nav.setBackgroundColor(Color.rgb(8,14,18));
        String[] ns={"⌂\nDomů","▣\nZakázky","＋\nNový záznam","●\nChat","☰\nMenu"};
        for(String n:ns){ TextView t=navItem(n); nav.addView(t,new LinearLayout.LayoutParams(0,dp(68),1)); }
        FrameLayout.LayoutParams np=new FrameLayout.LayoutParams(-1,dp(78),Gravity.BOTTOM); root.addView(nav,np); setContentView(root);
        content.setPadding(0,0,0,dp(78));
    }
    TextView navItem(String s){ TextView t=new TextView(this); t.setText(s); t.setTextColor(TEXT); t.setTextSize(12); t.setGravity(Gravity.CENTER); t.setTypeface(Typeface.DEFAULT,Typeface.BOLD); t.setOnClickListener(v->{String x=((TextView)v).getText().toString(); if(x.contains("Domů"))show("Domů"); else if(x.contains("Zakázky"))show("Zakázky"); else if(x.contains("Chat"))show("Chat"); else if(x.contains("Nový"))newRecord(); else menu();}); return t; }
    void show(String s){ section=s; content.setSection(s); }
    void newRecord(){ visits++; planned=Math.max(0,planned-1); active++; save(); Toast.makeText(this,"Nový záznam uložen",Toast.LENGTH_SHORT).show(); content.invalidate(); }
    void menu(){ new AlertDialog.Builder(this).setTitle("SOMSI – Menu").setItems(new String[]{"Adresář","Fotodokumentace","Stavební deník","Úkoly","Docházka","Materiál","Dokumenty","Nastavení"},(d,w)->{String[] a={"Adresář","Fotodokumentace","Stavební deník","Úkoly","Docházka","Materiál","Dokumenty","Nastavení"}; if(w==0)show(a[w]); else if(w==1)camera(); else show(a[w]);}).show(); }
    void camera(){ try{Intent i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE); startActivityForResult(i,10);}catch(Exception e){Toast.makeText(this,"Fotoaparát není dostupný",Toast.LENGTH_SHORT).show();} }
    void call(){ Intent i=new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+420000000000")); startActivity(i); }
    void chat(){ final EditText in=new EditText(this); in.setHint("Napište zprávu…"); new AlertDialog.Builder(this).setTitle("Nová zpráva").setView(in).setNegativeButton("Zrušit",null).setPositiveButton("Odeslat",(d,w)->{if(in.getText().length()>0)Toast.makeText(this,"Zpráva odeslána",Toast.LENGTH_SHORT).show();}).show(); }
    int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}

    class SomsiView extends View {
        Paint p=new Paint(3); String sec="Domů"; RectF r=new RectF();
        SomsiView(Context c){super(c); p.setTypeface(Typeface.create("sans",Typeface.NORMAL)); setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        void setSection(String s){sec=s; invalidate();}
        void txt(Canvas c,String s,float x,float y,float size,int color,boolean bold){p.setTextSize(dp((int)size));p.setColor(color);p.setTypeface(Typeface.create("sans",bold?Typeface.BOLD:Typeface.NORMAL));c.drawText(s,dp((int)x),dp((int)y),p);}
        void box(Canvas c,float l,float t,float rr,float bb,int color,float rad){p.setColor(color);p.setStyle(Paint.Style.FILL);r.set(dp((int)l),dp((int)t),dp((int)rr),dp((int)bb));c.drawRoundRect(r,dp((int)rad),dp((int)rad),p);}
        @Override protected void onDraw(Canvas c){super.onDraw(c); c.drawColor(DARK); if(sec.equals("Domů"))home(c); else section(c);}
        void home(Canvas c){
            // Hero
            box(c,0,0,1000,315,Color.rgb(10,24,31),0); p.setShader(new LinearGradient(0,0,0,dp(315),Color.rgb(8,18,24),Color.rgb(31,42,39),Shader.TileMode.CLAMP)); c.drawRect(0,0,getWidth(),dp(315),p); p.setShader(null);
            // stylized house illustration
            p.setColor(Color.rgb(46,58,60)); c.drawRect(dp(80),dp(160),getWidth()-dp(70),dp(285),p); Path roof=new Path(); roof.moveTo(dp(55),dp(165));roof.lineTo(getWidth()/2,dp(70));roof.lineTo(getWidth()-dp(40),dp(165));roof.close();p.setColor(Color.rgb(62,70,68));c.drawPath(roof,p);
            p.setColor(Color.rgb(28,37,40));c.drawRect(dp(135),dp(190),dp(230),dp(275),p);c.drawRect(getWidth()-dp(230),dp(190),getWidth()-dp(135),dp(275),p);p.setColor(GOLD);c.drawRect(dp(165),dp(215),dp(205),dp(275),p);
            // dark overlay
            p.setShader(new LinearGradient(0,0,0,dp(315),0x22000000,0xCC000000,Shader.TileMode.CLAMP));c.drawRect(0,0,getWidth(),dp(315),p);p.setShader(null);
            txt(c,"☰",24,50,30,TEXT,false); txt(c,"SOMSI",180,72,34,TEXT,true); txt(c,"stavební práce",182,106,18,TEXT,false); txt(c,"Od základu po střechu",160,145,20,GOLD,true);
            txt(c,"KVALITA",720,70,11,MUTED,false);txt(c,"ZKUŠENOSTI",720,94,11,MUTED,false);txt(c,"SPOLEHLIVOST",720,118,11,MUTED,false);
            txt(c,"Stavíme",735,205,22,TEXT,true);txt(c,"Vaše plány",735,232,22,TEXT,true);txt(c,"do reality",735,259,22,TEXT,true);
            stats(c); grid(c);
        }
        void stats(Canvas c){int top=330; String[] a={"👥","Návštěv aplikace",""+visits,"+12% ↗"}; String[] b={"◒","Aktivních zakázek",""+active,"+8% ↗"}; String[] d={"✓","Dokončených",""+done,"zakázek"}; String[] e={"▣","Plánovaných",""+planned,"zakázek"}; String[][] all={a,b,d,e}; for(int i=0;i<4;i++){float l=15+i*187;box(c,l,top,l+174,top+105,PANEL,16);txt(c,all[i][0],l+68,top+31,25,GOLD,true);txt(c,all[i][2],l+55,top+67,22,TEXT,true);txt(c,all[i][1],l+25,top+91,12,TEXT,false);if(i<2)txt(c,all[i][3],l+66,top+104,11,Color.rgb(112,230,112),true);}}
        void grid(Canvas c){String[] names={"Zakázky","Fotodokumentace","Stavební deník","Úkoly","Docházka","Materiál","Dokumenty","Adresář","Chat"};String[] icons={"▣","●","▤","✓","👥","◆","▤","♟","●"};int y=455;for(int i=0;i<9;i++){int row=i/3,col=i%3;float l=15+col*250;float t=y+row*112;box(c,l,t,l+235,t+98,i==0?Color.rgb(164,112,26):PANEL,16);txt(c,icons[i],l+22,t+48,25,i==0?TEXT:GOLD,true);txt(c,names[i],l+64,t+49,15,TEXT,true);txt(c,"›",l+211,t+54,23,GOLD,true);if(i==8) {box(c,l+196,t+4,l+230,t+34,Color.rgb(238,75,50),17);txt(c,"3",l+207,t+25,12,TEXT,true);}} callPanel(c);}
        void callPanel(Canvas c){int y=810;box(c,15,y,985,y+92,Color.rgb(17,25,29),15);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(2));p.setColor(GOLD);c.drawRoundRect(new RectF(dp(15),dp(y),getWidth()-dp(15),dp(y+92)),dp(15),dp(15),p);p.setStyle(Paint.Style.FILL);txt(c,"☎",38,y+53,27,GOLD,true);txt(c,"Potřebujete konzultaci?",82,y+38,16,TEXT,true);txt(c,"Ozvěte se nám!",82,y+63,14,MUTED,false);box(c,610,y+18,900,y+72,GOLD,12);txt(c,"☎  Zavolat",675,y+52,17,Color.BLACK,true);
            txt(c,"⌖  Sídlo firmy",40,y+145,13,MUTED,false);txt(c,"Horní Studenec 29",40,y+170,15,TEXT,true);txt(c,"538 43 Třemošnice",40,y+193,14,TEXT,false);txt(c,"◷  Působíme v regionech",430,y+145,13,MUTED,false);txt(c,"Pardubický kraj",430,y+170,14,TEXT,true);txt(c,"Královéhradecký kraj",430,y+191,14,TEXT,false);txt(c,"Vysočina",430,y+212,14,TEXT,false); }
        void section(Canvas c){
            txt(c,"‹",20,48,32,GOLD,true); txt(c,sec,70,45,24,TEXT,true); txt(c,"SOMSI",760,45,20,GOLD,true);
            box(c,20,70,980,190,PANEL,18);
            if(sec.equals("Zakázky")){txt(c,"AKTIVNÍ ZAKÁZKY",45,105,13,MUTED,true);txt(c,"86",45,150,34,TEXT,true);txt(c,"Novostavba – Horní Studenec",250,108,17,TEXT,true);txt(c,"Fasáda a zateplení",250,138,14,MUTED,false);txt(c,"Dokončení: 30. 10. 2026",250,165,13,GOLD,true);}
            else if(sec.equals("Adresář")){txt(c,"KONTAKTY",45,105,13,MUTED,true);txt(c,"Jan Novák",45,140,18,TEXT,true);txt(c,"stavbyvedoucí · +420 000 000 001",45,165,13,MUTED,false);txt(c,"Petr Svoboda",45,195,18,TEXT,true);txt(c,"zedník · +420 000 000 002",45,220,13,MUTED,false);}
            else if(sec.equals("Chat")){txt(c,"TEAM CHAT",45,105,13,MUTED,true);txt(c,"●  Stavbyvedoucí",45,145,16,TEXT,true);txt(c,"Materiál dorazí zítra v 8:00.",45,173,14,MUTED,false);txt(c,"●  Petr",45,205,16,TEXT,true);txt(c,"Fotky z dneška jsou nahrané.",45,233,14,MUTED,false);box(c,45,255,955,305,Color.rgb(33,43,48),12);txt(c,"Napsat zprávu…",65,288,14,MUTED,false);box(c,800,263,940,300,GOLD,10);txt(c,"Odeslat",835,288,14,Color.BLACK,true);}
            else {txt(c,"PŘEHLED",45,105,13,MUTED,true);txt(c,"Sekce "+sec,45,145,24,TEXT,true);txt(c,"Funkce jsou připravené pro práci v telefonu.",45,175,14,MUTED,false);}
            txt(c,"›",930,145,30,GOLD,true);
        }
        @Override public boolean onTouchEvent(android.view.MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_UP)return true;float x=e.getX()/getResources().getDisplayMetrics().density,y=e.getY()/getResources().getDisplayMetrics().density;if(sec.equals("Domů")){if(y>455&&y<790){int row=(int)((y-455)/112),col=(int)(x/250);int i=row*3+col;if(i>=0&&i<9){String[] n={"Zakázky","Fotodokumentace","Stavební deník","Úkoly","Docházka","Materiál","Dokumenty","Adresář","Chat"};if(i==1)camera();else if(i==8)chat();else show(n[i]);}} if(y>810&&y<910&&x>580)call();}else{if(y<70)show("Domů");if(sec.equals("Chat")&&y>250&&y<330)chat();}return true;}
    }
}
