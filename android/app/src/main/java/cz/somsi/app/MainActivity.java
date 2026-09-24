package cz.somsi.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.*;

public class MainActivity extends Activity {
    private static final String WEB = "https://stavbysomsi.cz/";
    private static final String PHONE = "+420736771754";
    private static final int BG = Color.rgb(7, 12, 16);
    private static final int GOLD = Color.rgb(245, 178, 43);
    private static final int PANEL = Color.rgb(20, 29, 35);
    private static final int PANEL_2 = Color.rgb(27, 38, 45);
    private LinearLayout content;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        try { requestWindowFeature(Window.FEATURE_NO_TITLE); } catch (Throwable ignored) {}
        configureWindow();
        showHome();
    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) configureWindow();
    }

    private void configureWindow() {
        try {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (Build.VERSION.SDK_INT >= 21) {
                getWindow().setStatusBarColor(BG);
                getWindow().setNavigationBarColor(BG);
            }
            if (Build.VERSION.SDK_INT >= 30) {
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
        } catch (Throwable ignored) {}
    }

    private void showHome() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(18), dp(18), dp(18), dp(28));
        content.setBackgroundColor(BG);
        scroll.addView(content);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView logo = label("SOMSI", 30, GOLD, true);
        header.addView(logo, new LinearLayout.LayoutParams(0, dp(54), 1));
        Button menu = smallButton("☰");
        menu.setOnClickListener(v -> showMenu());
        header.addView(menu, new LinearLayout.LayoutParams(dp(54), dp(54)));
        content.addView(header);

        TextView subtitle = label("STAVEBNÍ PRÁCE", 13, Color.LTGRAY, true);
        subtitle.setLetterSpacing(.18f);
        content.addView(subtitle, margin(0, 0, 0, 12));

        LinearLayout hero = panel();
        hero.setPadding(dp(14), dp(14), dp(14), dp(14));
        TextView h1 = label("Od základu po střechu", 24, Color.WHITE, true);
        hero.addView(h1);
        TextView h2 = label("Zakázky, fotodokumentace a stavební agenda na jednom místě.", 14, Color.LTGRAY, false);
        hero.addView(h2, margin(0, 6, 0, 12));
        Button web = actionButton("WEB SOMSI", GOLD);
        web.setOnClickListener(v -> openWeb());
        hero.addView(web, new LinearLayout.LayoutParams(-1, dp(50)));
        content.addView(hero, margin(0, 0, 0, 14));

        TextView section = label("RYCHLÝ PŘÍSTUP", 13, GOLD, true);
        section.setLetterSpacing(.12f);
        content.addView(section, margin(2, 0, 0, 8));

        addGridRow("Zakázky", "86 aktivních", "📋", () -> openSection("Zakázky"),
                   "Fotodokumentace", "Nová fotografie", "📷", this::openCamera);
        addGridRow("Stavební deník", "Záznamy stavby", "📒", () -> openSection("Stavební deník"),
                   "Úkoly", "Denní úkoly", "✓", () -> openSection("Úkoly"));
        addGridRow("Docházka", "Příchod / odchod", "⏱", () -> openSection("Docházka"),
                   "Materiál", "Sklad a materiál", "▣", () -> openSection("Materiál"));
        addGridRow("Dokumenty", "Smlouvy a rozpočty", "▤", () -> openSection("Dokumenty"),
                   "Adresář", "Kontakty", "👤", () -> openSection("Adresář"));

        LinearLayout bottom = panel();
        bottom.setPadding(dp(12), dp(12), dp(12), dp(12));
        Button chat = actionButton("💬  Otevřít chat", Color.WHITE);
        chat.setOnClickListener(v -> showChat());
        bottom.addView(chat, new LinearLayout.LayoutParams(-1, dp(52)));
        Button call = actionButton("📞  Zavolat SOMSI", GOLD);
        call.setOnClickListener(v -> callPhone());
        bottom.addView(call, margin(0, 8, 0, 0));
        content.addView(bottom, margin(0, 14, 0, 0));

        TextView foot = label("SOMSI stavební práce  •  +420 736 771 754", 12, Color.GRAY, false);
        foot.setGravity(Gravity.CENTER);
        content.addView(foot, margin(0, 16, 0, 0));

        setContentView(scroll);
        configureWindow();
    }

    private void addGridRow(String a, String aSub, String aIcon, Runnable aRun,
                            String b, String bSub, String bIcon, Runnable bRun) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.addView(tile(a, aSub, aIcon, aRun), new LinearLayout.LayoutParams(0, dp(116), 1));
        Space gap = new Space(this);
        row.addView(gap, new LinearLayout.LayoutParams(dp(8), 1));
        row.addView(tile(b, bSub, bIcon, bRun), new LinearLayout.LayoutParams(0, dp(116), 1));
        content.addView(row, margin(0, 0, 0, 8));
    }

    private LinearLayout tile(String title, String sub, String icon, Runnable action) {
        LinearLayout box = panel();
        box.setPadding(dp(12), dp(10), dp(10), dp(10));
        box.setOnClickListener(v -> action.run());
        TextView ic = label(icon, 23, GOLD, true);
        box.addView(ic, new LinearLayout.LayoutParams(-1, dp(30)));
        TextView t = label(title, 16, Color.WHITE, true);
        box.addView(t);
        TextView s = label(sub, 12, Color.LTGRAY, false);
        box.addView(s, margin(0, 3, 0, 0));
        return box;
    }

    private LinearLayout panel() {
        LinearLayout p = new LinearLayout(this);
        p.setOrientation(LinearLayout.VERTICAL);
        p.setBackgroundColor(PANEL);
        return p;
    }

    private Button actionButton(String text, int color) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(15);
        b.setTextColor(color == GOLD ? Color.BLACK : Color.WHITE);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setAllCaps(false);
        b.setBackgroundColor(color == GOLD ? GOLD : PANEL_2);
        return b;
    }

    private Button smallButton(String text) {
        Button b = actionButton(text, Color.WHITE);
        b.setTextSize(22);
        b.setPadding(0, 0, 0, 0);
        return b;
    }

    private TextView label(String text, float size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setTypeface(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL);
        t.setGravity(Gravity.CENTER_VERTICAL);
        return t;
    }

    private LinearLayout.LayoutParams margin(int l, int t, int r, int b) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(dp(l), dp(t), dp(r), dp(b));
        return p;
    }

    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }

    private void showMenu() {
        final String[] items = {"🌐  Webové stránky SOMSI", "📞  Zavolat", "💬  Chat", "📷  Fotodokumentace", "📋  Zakázky", "👤  Adresář", "ℹ️  O aplikaci"};
        new AlertDialog.Builder(this).setTitle("SOMSI stavební práce").setItems(items, (d, which) -> {
            switch (which) { case 0: openWeb(); break; case 1: callPhone(); break; case 2: showChat(); break; case 3: openCamera(); break; case 4: openSection("Zakázky"); break; case 5: openSection("Adresář"); break; default: showAbout(); }
        }).setNegativeButton("Zavřít", null).show();
    }

    private void showAbout() {
        new AlertDialog.Builder(this).setTitle("SOMSI stavební práce")
            .setMessage("Od základu po střechu\n\nAplikace SOMSI pro zakázky, fotodokumentaci, stavební deník, úkoly, docházku, materiál, dokumenty, adresář a komunikaci.")
            .setPositiveButton("OK", null).show();
    }

    private void callPhone() { try { startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + PHONE))); } catch (Throwable e) { Toast.makeText(this, "Telefon není dostupný", Toast.LENGTH_SHORT).show(); } }
    private void openWeb() { try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(WEB))); } catch (Throwable e) { Toast.makeText(this, "Web nelze otevřít", Toast.LENGTH_SHORT).show(); } }
    private void openCamera() { try { startActivity(new Intent(MediaStore.ACTION_IMAGE_CAPTURE)); } catch (Throwable e) { Toast.makeText(this, "Fotoaparát není dostupný", Toast.LENGTH_SHORT).show(); } }

    private void openSection(String title) {
        ScrollView scroll = new ScrollView(this);
        LinearLayout page = new LinearLayout(this); page.setOrientation(LinearLayout.VERTICAL); page.setPadding(dp(18), dp(18), dp(18), dp(24)); page.setBackgroundColor(BG); scroll.addView(page);
        LinearLayout header = new LinearLayout(this); header.setGravity(Gravity.CENTER_VERTICAL);
        Button back = smallButton("‹"); back.setOnClickListener(v -> showHome()); header.addView(back, new LinearLayout.LayoutParams(dp(54), dp(54)));
        header.addView(label(title, 23, Color.WHITE, true), new LinearLayout.LayoutParams(0, dp(54), 1)); page.addView(header, margin(0,0,0,14));
        String[] lines;
        switch(title){ case "Zakázky": lines=new String[]{"Aktivní zakázky • 86","Rekonstrukce domu – Třemošnice","Hrubá stavba – Pardubice","Fasáda – Vysočina","Plánované realizace – 12"}; break; case "Stavební deník": lines=new String[]{"Dnešní záznam","Postup prací","Fotografie","Poznámky stavby","Přidat nový záznam"}; break; case "Úkoly": lines=new String[]{"Dnešní úkoly","Kontrola materiálu","Dokončit omítky","Objednat okna","Kontrola předání"}; break; case "Docházka": lines=new String[]{"Dnešní docházka","Přihlásit příchod","Přihlásit odchod","Historie docházky"}; break; case "Materiál": lines=new String[]{"Sklad a materiál","Cement • 48 ks","Izolace • 24 balení","Dlažba • 36 m²","Přidat materiál"}; break; case "Dokumenty": lines=new String[]{"Dokumenty zakázek","Smlouvy","Rozpočty","Předávací protokoly","Faktury"}; break; case "Adresář": lines=new String[]{"Kontakty","SOMSI – kancelář","Stavbyvedoucí","Dodavatelé","Klienti"}; break; default: lines=new String[]{"Přehled","Nový záznam","Historie","Nastavení"}; }
        for (String line : lines) { Button b = actionButton(line, Color.WHITE); b.setGravity(Gravity.CENTER_VERTICAL); b.setOnClickListener(v -> Toast.makeText(this, line + " – připraveno", Toast.LENGTH_SHORT).show()); page.addView(b, margin(0,0,0,8)); }
        setContentView(scroll); configureWindow();
    }

    private void showChat() {
        LinearLayout page=new LinearLayout(this); page.setOrientation(LinearLayout.VERTICAL); page.setPadding(dp(18),dp(18),dp(18),dp(12)); page.setBackgroundColor(BG);
        LinearLayout header=new LinearLayout(this); header.setGravity(Gravity.CENTER_VERTICAL); Button back=smallButton("‹"); back.setOnClickListener(v->showHome()); header.addView(back,new LinearLayout.LayoutParams(dp(54),dp(54))); header.addView(label("Chat SOMSI",23,Color.WHITE,true),new LinearLayout.LayoutParams(0,dp(54),1)); page.addView(header,margin(0,0,0,10));
        TextView messages=label("Petr • 09:12\nZakázka Třemošnice – práce pokračují podle plánu.\n\nVy • 09:18\nDěkuji, potvrzuji.\n\nJana • 09:26\nNové fotografie jsou nahrané.",16,Color.WHITE,false); messages.setBackgroundColor(PANEL); messages.setPadding(dp(16),dp(16),dp(16),dp(16)); page.addView(messages,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout compose=new LinearLayout(this); EditText input=new EditText(this); input.setHint("Napište zprávu…"); input.setTextColor(Color.WHITE); input.setHintTextColor(Color.GRAY); compose.addView(input,new LinearLayout.LayoutParams(0,dp(58),1)); Button send=actionButton("Odeslat",GOLD); send.setOnClickListener(v->{if(input.getText().length()>0){messages.append("\n\nVy • nyní\n"+input.getText());input.setText("");}}); compose.addView(send,new LinearLayout.LayoutParams(dp(110),dp(58))); page.addView(compose,margin(0,8,0,0)); setContentView(page); configureWindow();
    }
}
