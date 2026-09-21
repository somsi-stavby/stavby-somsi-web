package cz.somsi.app;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    static final int GOLD=Color.rgb(242,174,43), GOLD2=Color.rgb(255,196,75);
    static final int BG=Color.rgb(5,11,15), PANEL=Color.rgb(16,24,29), PANEL2=Color.rgb(23,33,39);
    static final int WHITE=Color.WHITE, MUTED=Color.rgb(190,198,203), GREEN=Color.rgb(91,214,112), RED=Color.rgb(239,78,58);
    static final String WEB="https://stavbysomsi.cz/", PHONE="+420736771754";
    LinearLayout page; SharedPreferences prefs; TextView visitsText,jobsText,doneText,planText;
    int visits,jobs,done,planned;

    int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    TextView tv(String s,float sp,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(sp);t.setTextColor(color);t.setTypeface(Typeface.create("sans",bold?Typeface.BOLD:Typeface.NORMAL));t.setGravity(Gravity.CENTER_VERTICAL);return t;}
    GradientDrawable bg(int color,float r){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(r));return g;}
    GradientDrawable outline(int color,int width,float r){GradientDrawable g=bg(Color.TRANSPARENT,r);g.setStroke(dp(width),color);return g;}
    LinearLayout box(int color,float r){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setBackground(bg(color,r));return l;}
    void pad(View v,int l,int t,int r,int b){v.setPadding(dp(l),dp(t),dp(r),dp(b));}

    @Override public void onCreate(Bundle state){
        super.onCreate(state); getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);
        if(Build.VERSION.SDK_INT>=29)getWindow().setNavigationBarContrastEnforced(false);
        prefs=getSharedPreferences("somsi",MODE_PRIVATE);visits=prefs.getInt("visits",1248);jobs=prefs.getInt("jobs",86);done=prefs.getInt("done",67);planned=prefs.getInt("planned",12);build();
    }

    void build(){
        FrameLayout root=new FrameLayout(this);root.setBackgroundColor(BG);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(false);scroll.setClipToPadding(false);scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(10),0,dp(10),dp(88));scroll.addView(page,new ScrollView.LayoutParams(-1,-2));
        root.addView(scroll,new FrameLayout.LayoutParams(-1,-1));root.addView(bottomBar(),new FrameLayout.LayoutParams(-1,dp(76),Gravity.BOTTOM));setContentView(root);buildHome();
    }

    FrameLayout hero(){
        FrameLayout h=new FrameLayout(this);
        ImageView img=new ImageView(this);img.setImageResource(R.drawable.realizace_hlavni);img.setScaleType(ImageView.ScaleType.CENTER_CROP);h.addView(img,new FrameLayout.LayoutParams(-1,dp(365)));
        View shade=new View(this);shade.setBackgroundColor(0x52000000);h.addView(shade,new FrameLayout.LayoutParams(-1,dp(365)));
        TextView menu=tv("☰",31,WHITE,false);menu.setGravity(Gravity.CENTER);menu.setOnClickListener(v->showMenu());FrameLayout.LayoutParams ml=new FrameLayout.LayoutParams(dp(58),dp(58));ml.leftMargin=dp(2);ml.topMargin=dp(8);h.addView(menu,ml);
        TextView web=tv("WEB",13,GOLD2,true);web.setGravity(Gravity.CENTER);web.setBackground(bg(0xB5050B0F,16));web.setOnClickListener(v->openWeb());FrameLayout.LayoutParams wl=new FrameLayout.LayoutParams(dp(70),dp(48),Gravity.RIGHT);wl.rightMargin=dp(8);wl.topMargin=dp(10);h.addView(web,wl);
        TextView bell=tv("♧",28,WHITE,true);bell.setGravity(Gravity.CENTER);FrameLayout.LayoutParams bl=new FrameLayout.LayoutParams(dp(46),dp(46),Gravity.RIGHT);bl.rightMargin=dp(82);bl.topMargin=dp(10);h.addView(bell,bl);
        TextView badge=tv("3",10,WHITE,true);badge.setGravity(Gravity.CENTER);badge.setBackground(bg(RED,12));FrameLayout.LayoutParams bd=new FrameLayout.LayoutParams(dp(22),dp(22),Gravity.RIGHT);bd.rightMargin=dp(80);bd.topMargin=dp(8);h.addView(badge,bd);

        LinearLayout logo=new LinearLayout(this);logo.setOrientation(LinearLayout.VERTICAL);logo.setGravity(Gravity.CENTER_HORIZONTAL);
        TextView roof=tv("⌃",58,GOLD2,true);roof.setGravity(Gravity.CENTER);roof.setIncludeFontPadding(false);
        TextView name=tv("SOMSI",42,WHITE,true);name.setGravity(Gravity.CENTER);name.setIncludeFontPadding(false);
        TextView sub=tv("stavební práce",20,WHITE,false);sub.setGravity(Gravity.CENTER);
        TextView motto=tv("Od základu po střechu",20,GOLD2,true);motto.setGravity(Gravity.CENTER);logo.addView(roof,new LinearLayout.LayoutParams(-1,dp(45)));logo.addView(name,new LinearLayout.LayoutParams(-1,dp(51)));logo.addView(sub,new LinearLayout.LayoutParams(-1,dp(30)));logo.addView(motto,new LinearLayout.LayoutParams(-1,dp(38)));
        FrameLayout.LayoutParams ll=new FrameLayout.LayoutParams(dp(255),dp(168),Gravity.TOP|Gravity.CENTER_HORIZONTAL);ll.topMargin=dp(30);h.addView(logo,ll);

        LinearLayout virtues=new LinearLayout(this);virtues.setOrientation(LinearLayout.VERTICAL);String[] v={"KVALITA","ZKUŠENOSTI","SPOLEHLIVOST","VAŠE STAVBA","NAŠE PRIORITA"};for(String s:v){TextView x=tv(s,9,MUTED,true);x.setGravity(Gravity.LEFT);virtues.addView(x,new LinearLayout.LayoutParams(-1,dp(24)));}
        FrameLayout.LayoutParams vl=new FrameLayout.LayoutParams(dp(120),dp(126),Gravity.RIGHT);vl.rightMargin=dp(8);vl.topMargin=dp(136);h.addView(virtues,vl);
        LinearLayout slogan=new LinearLayout(this);slogan.setOrientation(LinearLayout.VERTICAL);slogan.setGravity(Gravity.RIGHT);slogan.addView(tv("Stavíme",22,WHITE,true));slogan.addView(tv("Vaše plány",22,WHITE,true));slogan.addView(tv("do reality",22,WHITE,true));TextView u=tv("━━━━━━━━",12,GOLD2,true);u.setGravity(Gravity.RIGHT);slogan.addView(u);
        FrameLayout.LayoutParams sl=new FrameLayout.LayoutParams(dp(155),dp(120),Gravity.RIGHT);sl.rightMargin=dp(8);sl.topMargin=dp(244);h.addView(slogan,sl);return h;
    }

    void buildHome(){page.removeAllViews();page.addView(hero(),new LinearLayout.LayoutParams(-1,dp(365)));page.addView(stats(),new LinearLayout.LayoutParams(-1,dp(112)));page.addView(tileGrid(),new LinearLayout.LayoutParams(-1,dp(318)));page.addView(callPanel(),new LinearLayout.LayoutParams(-1,dp(94)));page.addView(webPanel(),new LinearLayout.LayoutParams(-1,dp(88)));page.addView(contactPanel(),new LinearLayout.LayoutParams(-1,dp(122)));}

    LinearLayout stats(){
        LinearLayout row=new LinearLayout(this);row.setPadding(0,dp(16),0,0);row.setGravity(Gravity.CENTER);row.setWeightSum(4);
        visitsText=stat(row,""+visits,"Návštěvy","+12% ↗");jobsText=stat(row,""+jobs,"Zakázky","+8% ↗");doneText=stat(row,""+done,"Dokončeno","");planText=stat(row,""+planned,"Plán","");return row;
    }
    TextView stat(LinearLayout row,String val,String label,String trend){
        LinearLayout c=box(PANEL,15);c.setGravity(Gravity.CENTER);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(96),1);lp.setMargins(dp(3),0,dp(3),0);row.addView(c,lp);
        TextView v=tv(val,18,WHITE,true);v.setGravity(Gravity.CENTER);c.addView(v,new LinearLayout.LayoutParams(-1,dp(34)));TextView n=tv(label,9,MUTED,false);n.setGravity(Gravity.CENTER);c.addView(n,new LinearLayout.LayoutParams(-1,dp(23)));if(!trend.isEmpty()){TextView tr=tv(trend,8,GREEN,true);tr.setGravity(Gravity.CENTER);c.addView(tr,new LinearLayout.LayoutParams(-1,dp(20)));}return v;
    }

    LinearLayout tileGrid(){
        LinearLayout grid=new LinearLayout(this);grid.setOrientation(LinearLayout.VERTICAL);grid.setPadding(0,dp(6),0,dp(6));String[][] d={{"▣","Zakázky"},{"◉","Fotodokumentace"},{"▤","Stavební deník"},{"✓","Úkoly"},{"●","Docházka"},{"◆","Materiál"},{"▤","Dokumenty"},{"♟","Adresář"},{"●","Chat"}};
        for(int r=0;r<3;r++){LinearLayout row=new LinearLayout(this);row.setWeightSum(3);row.setGravity(Gravity.CENTER_VERTICAL);grid.addView(row,new LinearLayout.LayoutParams(-1,dp(102)));for(int c=0;c<3;c++){int i=r*3+c;row.addView(tile(d[i][0],d[i][1],i==0,i==8),tileLp());}}return grid;
    }
    LinearLayout.LayoutParams tileLp(){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(96),1);p.setMargins(dp(3),dp(3),dp(3),dp(3));return p;}

    View tile(String sym,String name,boolean active,boolean chat){
        if(!chat){LinearLayout b=tileBody(sym,name,active);b.setOnClickListener(v->openSection(name));return b;}
        FrameLayout wrap=new FrameLayout(this);LinearLayout b=tileBody(sym,name,false);wrap.addView(b,new FrameLayout.LayoutParams(-1,-1));TextView n=tv("3",9,WHITE,true);n.setGravity(Gravity.CENTER);n.setBackground(bg(RED,12));FrameLayout.LayoutParams np=new FrameLayout.LayoutParams(dp(22),dp(22),Gravity.RIGHT);np.topMargin=dp(3);np.rightMargin=dp(3);wrap.addView(n,np);wrap.setOnClickListener(v->chat());return wrap;
    }
    LinearLayout tileBody(String sym,String name,boolean active){
        LinearLayout b=box(active?Color.rgb(185,126,18):PANEL,16);b.setOrientation(LinearLayout.HORIZONTAL);b.setGravity(Gravity.CENTER_VERTICAL);pad(b,8,8,5,8);
        TextView ic=tv(sym,23,active?Color.BLACK:GOLD2,true);ic.setGravity(Gravity.CENTER);ic.setBackground(bg(active?Color.rgb(250,199,88):Color.rgb(40,54,61),12));b.addView(ic,new LinearLayout.LayoutParams(dp(43),dp(43)));
        TextView label=tv(name,10,WHITE,true);label.setSingleLine(true);label.setEllipsize(android.text.TextUtils.TruncateAt.END);pad(label,7,0,0,0);b.addView(label,new LinearLayout.LayoutParams(0,dp(45),1));TextView ar=tv("›",23,GOLD2,true);ar.setGravity(Gravity.CENTER);b.addView(ar,new LinearLayout.LayoutParams(dp(20),dp(45)));return b;
    }

    LinearLayout callPanel(){
        LinearLayout p=box(PANEL2,16);p.setOrientation(LinearLayout.HORIZONTAL);p.setGravity(Gravity.CENTER_VERTICAL);p.setBackground(outline(GOLD2,1,16));pad(p,8,8,8,8);
        TextView ph=tv("☎",28,GOLD2,true);ph.setGravity(Gravity.CENTER);p.addView(ph,new LinearLayout.LayoutParams(dp(52),-1));LinearLayout tx=new LinearLayout(this);tx.setOrientation(LinearLayout.VERTICAL);tx.setGravity(Gravity.CENTER_VERTICAL);tx.addView(tv("Potřebujete konzultaci?",13,WHITE,true));tx.addView(tv("Ozvěte se nám!",11,MUTED,false));p.addView(tx,new LinearLayout.LayoutParams(0,-1,1));TextView call=tv("☎  Zavolat",13,Color.BLACK,true);call.setGravity(Gravity.CENTER);call.setBackground(bg(GOLD2,11));call.setOnClickListener(v->call());p.addView(call,new LinearLayout.LayoutParams(dp(120),dp(55)));return p;
    }
    LinearLayout webPanel(){
        LinearLayout p=box(PANEL,14);p.setOrientation(LinearLayout.HORIZONTAL);p.setGravity(Gravity.CENTER_VERTICAL);pad(p,8,6,8,6);p.setOnClickListener(v->openWeb());TextView i=tv("◎",27,GOLD2,true);i.setGravity(Gravity.CENTER);p.addView(i,new LinearLayout.LayoutParams(dp(48),-1));LinearLayout t=new LinearLayout(this);t.setOrientation(LinearLayout.VERTICAL);t.addView(tv("stavbysomsi.cz",13,WHITE,true));t.addView(tv("Oficiální web • realizace • služby • kontakt",9,MUTED,false));p.addView(t,new LinearLayout.LayoutParams(0,-1,1));TextView a=tv("›",25,GOLD2,true);a.setGravity(Gravity.CENTER);p.addView(a,new LinearLayout.LayoutParams(dp(28),-1));return p;
    }
    LinearLayout contactPanel(){
        LinearLayout p=box(PANEL2,14);p.setOrientation(LinearLayout.HORIZONTAL);p.setGravity(Gravity.CENTER_VERTICAL);pad(p,8,6,8,6);LinearLayout a=new LinearLayout(this);a.setOrientation(LinearLayout.VERTICAL);a.addView(tv("⌖  Sídlo firmy",11,WHITE,true));a.addView(tv("Horní Studenec 29",10,MUTED,false));a.addView(tv("538 43 Třemošnice",10,MUTED,false));p.addView(a,new LinearLayout.LayoutParams(0,-1,1));LinearLayout b=new LinearLayout(this);b.setOrientation(LinearLayout.VERTICAL);b.addView(tv("◷  Působíme v regionech",11,WHITE,true));b.addView(tv("Pardubický kraj",10,MUTED,false));b.addView(tv("Královéhradecký kraj • Vysočina",9,MUTED,false));p.addView(b,new LinearLayout.LayoutParams(0,-1,1));return p;
    }

    LinearLayout bottomBar(){
        LinearLayout bar=new LinearLayout(this);bar.setOrientation(LinearLayout.HORIZONTAL);bar.setGravity(Gravity.CENTER);bar.setBackgroundColor(Color.rgb(8,13,17));bar.setPadding(dp(4),dp(2),dp(4),dp(2));
        bar.addView(nav("⌂","Domů",()->top()),navLp());bar.addView(nav("▣","Zakázky",()->openSection("Zakázky")),navLp());
        TextView plus=tv("+",32,Color.BLACK,true);plus.setGravity(Gravity.CENTER);plus.setBackground(bg(GOLD2,35));plus.setOnClickListener(v->newRecord());LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(dp(62),dp(62));pp.setMargins(dp(4),0,dp(4),0);bar.addView(plus,pp);
        bar.addView(nav("●","Chat",()->chat()),navLp());bar.addView(nav("☰","Menu",()->showMenu()),navLp());return bar;
    }
    LinearLayout.LayoutParams navLp(){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(70),1);p.setMargins(dp(1),0,dp(1),0);return p;}
    View nav(String ico,String label,Runnable action){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);x.setGravity(Gravity.CENTER);TextView i=tv(ico,23,label.equals("Domů")?GOLD2:WHITE,true);i.setGravity(Gravity.CENTER);x.addView(i,new LinearLayout.LayoutParams(-1,dp(36)));TextView t=tv(label,9,label.equals("Domů")?GOLD2:WHITE,true);t.setGravity(Gravity.CENTER);x.addView(t,new LinearLayout.LayoutParams(-1,dp(25)));x.setOnClickListener(v->action.run());return x;}

    void top(){page.requestFocus();}
    void save(){prefs.edit().putInt("visits",visits).putInt("jobs",jobs).putInt("done",done).putInt("planned",planned).apply();}
    void call(){try{startActivity(new Intent(Intent.ACTION_DIAL,Uri.parse("tel:"+PHONE)));}catch(Exception e){Toast.makeText(this,"Telefon není dostupný",Toast.LENGTH_SHORT).show();}}
    void openWeb(){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(WEB)));}catch(Exception e){Toast.makeText(this,"Web nelze otevřít",Toast.LENGTH_SHORT).show();}}
    void camera(){try{startActivity(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));}catch(Exception e){Toast.makeText(this,"Fotoaparát není dostupný",Toast.LENGTH_SHORT).show();}}
    void openSection(String name){
        if(name.equals("Fotodokumentace")){camera();return;}if(name.equals("Chat")){chat();return;}
        String[] names={"Zakázky","Fotodokumentace","Stavební deník","Úkoly","Docházka","Materiál","Dokumenty","Adresář"};String[] text={"Přehled zakázek, stav realizací a plánované práce.","Fotografie realizací a dokumentace staveb.","Záznamy průběhu staveb a poznámky.","Úkoly pro jednotlivé zakázky.","Přehled docházky pracovníků.","Materiál, spotřeba a plánované dodávky.","Dokumenty, nabídky a podklady k zakázkám.","Kontakty na firmu a spolupracovníky."};int idx=Arrays.asList(names).indexOf(name);new AlertDialog.Builder(this).setTitle("SOMSI • "+name).setMessage(idx>=0?text[idx]:"Sekce SOMSI").setPositiveButton("OK",null).show();
    }
    void newRecord(){visits++;jobs++;save();refreshStats();Toast.makeText(this,"Nový záznam uložen",Toast.LENGTH_SHORT).show();}
    void refreshStats(){if(visitsText!=null){visitsText.setText(""+visits);jobsText.setText(""+jobs);doneText.setText(""+done);planText.setText(""+planned);}}
    void chat(){
        LinearLayout b=new LinearLayout(this);b.setOrientation(LinearLayout.VERTICAL);pad(b,12,4,12,0);TextView hist=tv(prefs.getString("chat","Žádné zprávy."),10,MUTED,false);hist.setBackground(bg(PANEL2,12));pad(hist,10,10,10,10);b.addView(hist,new LinearLayout.LayoutParams(-1,dp(125)));EditText input=new EditText(this);input.setHint("Napište zprávu…");input.setTextColor(WHITE);input.setHintTextColor(MUTED);b.addView(input,new LinearLayout.LayoutParams(-1,dp(55)));new AlertDialog.Builder(this).setTitle("SOMSI Chat").setView(b).setNegativeButton("Zavřít",null).setPositiveButton("Odeslat",(d,w)->{String m=input.getText().toString().trim();if(!m.isEmpty()){String old=prefs.getString("chat","");prefs.edit().putString("chat",(old.isEmpty()?"":old+"\n")+"Já: "+m).apply();Toast.makeText(this,"Zpráva uložena",Toast.LENGTH_SHORT).show();}}).show();
    }
    void showMenu(){final String[] items={"Zakázky","Fotodokumentace","Stavební deník","Úkoly","Docházka","Materiál","Dokumenty","Adresář","Otevřít web SOMSI","Zavolat SOMSI"};new AlertDialog.Builder(this).setTitle("SOMSI • Menu").setItems(items,(d,w)->{if(w==8)openWeb();else if(w==9)call();else openSection(items[w]);}).show();}
}
