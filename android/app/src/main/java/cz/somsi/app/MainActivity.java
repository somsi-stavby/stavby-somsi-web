package cz.somsi.app;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity {
    static final String WEB="https://stavbysomsi.cz/";
    static final String PHONE="+420736771754";
    HomeView home;

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);
        hideSystemBars();
        home=new HomeView(this);
        setContentView(home);
    }

    void hideSystemBars(){
        if(Build.VERSION.SDK_INT>=30){
            WindowInsetsController c=getWindow().getInsetsController();
            if(c!=null)c.hide(WindowInsets.Type.statusBars()|WindowInsets.Type.navigationBars());
        }else{
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    void openWeb(){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(WEB)));}catch(Exception e){toast("Web nelze otevřít");}}
    void call(){try{startActivity(new Intent(Intent.ACTION_DIAL,Uri.parse("tel:"+PHONE)));}catch(Exception e){toast("Telefon není dostupný");}}
    void camera(){try{startActivity(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));}catch(Exception e){toast("Fotoaparát není dostupný");}}
    void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}

    void menu(){
        final String[] items={"🌐  Otevřít web stavbysomsi.cz","☎  Zavolat","📋  Zakázky","💬  Chat","👤  Adresář","➕  Nový záznam"};
        new AlertDialog.Builder(this).setTitle("SOMSI").setItems(items,(d,w)->{
            if(w==0)openWeb(); else if(w==1)call(); else if(w==2)section("Zakázky"); else if(w==3)chat(); else if(w==4)section("Adresář"); else record();
        }).show();
    }
    void section(String name){
        if(name.equals("Fotodokumentace")){camera();return;}
        new AlertDialog.Builder(this).setTitle(name).setMessage("Sekce SOMSI „"+name+"“ je připravena k použití.").setPositiveButton("Zavřít",null).show();
    }
    void chat(){
        EditText input=new EditText(this);input.setHint("Napište zprávu…");
        new AlertDialog.Builder(this).setTitle("SOMSI Chat").setView(input).setNegativeButton("Zavřít",null).setPositiveButton("Odeslat",(d,w)->toast("Zpráva byla připravena k odeslání")).show();
    }
    void record(){
        final String[] types={"Zakázka","Fotografie","Úkol","Docházka","Materiál","Poznámka"};
        new AlertDialog.Builder(this).setTitle("Nový záznam").setItems(types,(d,w)->{if(w==1)camera();else toast("Nový záznam: "+types[w]);}).show();
    }

    class HomeView extends View {
        Bitmap bmp; Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG); float scale,left,top;
        HomeView(Context c){super(c);setBackgroundColor(Color.rgb(5,11,15));bmp=BitmapFactory.decodeResource(getResources(),R.drawable.somsi_ref);p.setFilterBitmap(true);setFocusable(true);}
        @Override protected void onDraw(Canvas c){
            super.onDraw(c);
            if(bmp==null)return;
            float sx=getWidth()/(float)bmp.getWidth(), sy=getHeight()/(float)bmp.getHeight();
            scale=Math.min(sx,sy);float w=bmp.getWidth()*scale,h=bmp.getHeight()*scale;
            left=(getWidth()-w)/2f;top=(getHeight()-h)/2f;
            c.drawBitmap(bmp,null,new RectF(left,top,left+w,top+h),p);
        }
        boolean hit(float x,float y,float l,float t,float r,float b){return x>=l&&x<=r&&y>=t&&y<=b;}
        @Override public boolean onTouchEvent(android.view.MotionEvent e){
            if(e.getAction()!=MotionEvent.ACTION_UP)return true;
            if(bmp==null||scale<=0)return true;
            float x=(e.getX()-left)/scale,y=(e.getY()-top)/scale;
            if(x<0||y<0||x>bmp.getWidth()||y>bmp.getHeight())return true;
            // Header controls
            if(hit(x,y,5,60,85,155)){menu();return true;}
            if(hit(x,y,660,45,755,170)){openWeb();return true;}
            // Statistics / 3x3 section grid
            float[] xs={0,255,505,759};
            String[][] names={{"Zakázky","Fotodokumentace","Stavební deník"},{"Úkoly","Docházka","Materiál"},{"Dokumenty","Adresář","Chat"}};
            float[] ys={710,830,945,1065};
            for(int r=0;r<3;r++)for(int col=0;col<3;col++){
                if(hit(x,y,xs[col]+4,ys[r]+4,xs[col+1]-4,ys[r+1]-4)){if(names[r][col].equals("Chat"))chat();else section(names[r][col]);return true;}
            }
            // Consultation / phone button
            if(hit(x,y,0,1065,759,1185)){call();return true;}
            // Web/contact area: official site is also directly tappable here.
            if(hit(x,y,0,1180,759,1375)){openWeb();return true;}
            // Bottom navigation
            if(hit(x,y,0,1370,145,1511)){return true;}
            if(hit(x,y,145,1370,300,1511)){section("Zakázky");return true;}
            if(hit(x,y,300,1340,465,1511)){record();return true;}
            if(hit(x,y,465,1370,620,1511)){chat();return true;}
            if(hit(x,y,620,1370,759,1511)){menu();return true;}
            return true;
        }
    }
}
