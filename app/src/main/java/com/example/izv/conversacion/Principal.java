package com.example.izv.conversacion;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;


public class Principal extends Activity implements TextToSpeech.OnInitListener{
    private int CTE = 1;
    final private int CTEHABLAR =2;
    private TextToSpeech tts;
    private TextView tv;
    private String habla, respuesta;
    private Button b, es, in;


    /***********************************************************************/
    /*                                                                     */
    /*                              METODOS ON                             */
    /*                                                                     */
    /***********************************************************************/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CTE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                tts = new TextToSpeech(this, this);
            } else {
                Intent intent = new Intent();
                intent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(intent);
            }
        }
        if(requestCode == CTEHABLAR && resultCode == RESULT_OK){
            ArrayList<String> textos = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            b.setEnabled(false);
            habla=textos.get(0);
            tv.append(habla+"\n");
            Hilo hilo = new Hilo();
            hilo.start();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_principal);
        Intent intent = new Intent();
        intent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(intent, CTE);
        tv=(TextView)findViewById(R.id.tv);
        b=(Button)findViewById(R.id.button);
        es=(Button)findViewById(R.id.es);
        in=(Button)findViewById(R.id.in);
        es.setEnabled(false);
    }

    /***********************************************************************/
    /*                                                                     */
    /*                          METODOS DE BOTONES                         */
    /*                                                                     */
    /***********************************************************************/

    public void hablar(View v){
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "es-ES");
        i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Habla ahora");
        i.putExtra(RecognizerIntent.
                        EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                3000);
        startActivityForResult(i, CTEHABLAR);
    }

    public void espanol(View view){
        tts.setLanguage(new Locale("es", "ES"));
        in.setEnabled(true);
        es.setEnabled(false);
    }

    public void ingles(View view){
        tts.setLanguage(Locale.US);
        in.setEnabled(false);
        es.setEnabled(true);
    }

    /***********************************************************************/
    /*                                                                     */
    /*                            METODOS DE TTS                           */
    /*                                                                     */
    /***********************************************************************/

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS){

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }

    /***********************************************************************/
    /*                                                                     */
    /*                                 HILOS                               */
    /*                                                                     */
    /***********************************************************************/

    private class Hilo extends Thread{
        @Override
        public void run(){
            try{
                ChatterBotFactory factory=new ChatterBotFactory();
                ChatterBot bot1=factory.create(ChatterBotType.CLEVERBOT);
                ChatterBotSession bot1session=bot1.createSession();
                respuesta= bot1session.think(habla);
                Verrespuesta verrespuesta = new Verrespuesta();
                Principal.this.runOnUiThread(verrespuesta);
            }catch (Exception e) {
                e.printStackTrace();
                Log.v("Charla: ",e.toString());
            }
        }
    }

    private class Verrespuesta extends Thread{
        @Override
        public void run(){
            tv.append(respuesta+"\n");
            tts.speak( respuesta, TextToSpeech.QUEUE_FLUSH, null );
            b.setEnabled(true);
        }
    }

}
