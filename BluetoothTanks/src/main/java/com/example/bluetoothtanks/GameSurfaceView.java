package com.example.bluetoothtanks;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.PowerManager;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.example.bluetoothtanks.framework.Circle;
import com.example.bluetoothtanks.framework.ColisionTests;
import com.example.bluetoothtanks.framework.Vector2;
import com.example.bluetoothtanks.framework.Rectangle;
import com.example.bluetoothtanks.framework.SpriteBatcher;
import com.example.bluetoothtanks.framework.Texture;
import com.example.bluetoothtanks.framework.TextureRegion;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Sasha on 9/15/13.
 */
public class GameSurfaceView extends GLSurfaceView {
//helloo
    private float SCLALE =1.5f;
    float FRUSTUM_WIDTH =SCLALE* 12.8f;
    float FRUSTUM_HEIGHT =SCLALE* 7.2f;
    private int ASTEROIDS_NUMBER=10;
    private long COLDAUN=200;//TIME BETWEN SHOOTING
   // MyGlVertices vertices,bullet,ballVertices;
    //MyGlTexture joistick,tank,bulletTexture;
    Texture atlas;
    TextureRegion joistickRegion,serverTankRegion,clientTankRegion,bulletRegion, asteroidRegion;
    SpriteBatcher spriteBatcher;
    public static boolean SERVER;

    final Vector2 touchPos = new Vector2();
    private PowerManager.WakeLock wl;
    private  long endTime,startTime,dt,lastShoot;
    Vector2 greenSquare = new Vector2(SCLALE*2,SCLALE*2);
    Vector2 centre = new Vector2(SCLALE*2,SCLALE*2);

   // float DX=0.01f;
    final private SimpleBluetoothGame activity;

    int fps=0;
    Rectangle smallWindow = new Rectangle(0,0,SCLALE*12.8f,SCLALE*7.2f);
    Rectangle bigWindow = new Rectangle(-6.4f,-3.6f,25.6f,14.4f);
    LinkedList<Bullet> bullets = new LinkedList<Bullet>();
    LinkedList<Bullet> deleteBullets = new LinkedList<Bullet>();
    LinkedList<Asteroid> asteroids = new LinkedList<Asteroid>();
    LinkedList<Asteroid> deleteAsteroids = new LinkedList<Asteroid>();
    //Iterator<Bullet> bulletIterator = bullets.iterator();
class DynamicGameObject{
        public Vector2 position;
        public Vector2 velociti;
        public float angle;
        public DynamicGameObject(Vector2 position, Vector2 velociti, float angle){
            this.position=position;
            this.angle=angle;
            this.velociti=velociti;
        }
        public void update(){
            this.position.add(velociti);
            if(this.position.x<smallWindow.lowerLeft.x){this.position.x=smallWindow.lowerLeft.x+smallWindow.width;}
            else if(this.position.y<smallWindow.lowerLeft.y){this.position.y=smallWindow.lowerLeft.y+smallWindow.height;}
            else if(this.position.y>smallWindow.lowerLeft.y+smallWindow.height){this.position.y=smallWindow.lowerLeft.y;}
            else if(this.position.x>smallWindow.lowerLeft.x+smallWindow.width){this.position.x=smallWindow.lowerLeft.x;}
        }
    }
    class Bullet extends DynamicGameObject{
        static final float ownSpeed=0.5f;

        public Bullet(Vector2 position, Vector2 velociti, float angle) {
            super(position, velociti, angle);
        }

        @Override
        public void update() {
            super.position.add(super.velociti);
        }
    }
    class Asteroid extends DynamicGameObject{
        float radius =0.5f;

        public Asteroid(Vector2 position, Vector2 velociti, float angle) {
            super(position, velociti, angle);
        }
    }

class Tank extends  DynamicGameObject {

    public Tank(Vector2 position, Vector2 velociti, float angle) {
        super(position, velociti, angle);
    }
}
    float angle=0;
    private boolean touchevent;
    Circle circle=new Circle(SCLALE*2f,SCLALE*2f,SCLALE*1);

    boolean LittleWindow = true;


    Tank serverTank;// = new Tank(new Vector2(2,2),new Vector2(0,0),0);
    Tank clientTank;//= new Tank(new Vector2(6,6),new Vector2(0,0),0);


    public synchronized void addBullet(Bullet bullet){
        bullets.add(bullet);
    }
    public GameSurfaceView(Context context, final SimpleBluetoothGame activity) {

        super(context);
        this.activity=activity;


        setRenderer(new Renderer() {

            @Override

            public void onSurfaceCreated(GL10 gl, EGLConfig config) {

                atlas=new Texture(gl,getResources(), "STARW.png");
                joistickRegion = new TextureRegion(atlas,64,0,64,64);

                //SELECT TANK POSITION
                if(BluetoothConnect.SERVER){
                    serverTankRegion=new TextureRegion(atlas,0,0,64,64);
                    clientTankRegion= new TextureRegion(atlas,192,0,64,64);
                    serverTank = new Tank(new Vector2(2,2),new Vector2(0,0),0);
                    clientTank= new Tank(new Vector2(10.8f,5.2f),new Vector2(0,0),0);
                }else{
                    clientTankRegion =new TextureRegion(atlas,0,0,64,64);
                     serverTankRegion= new TextureRegion(atlas,192,0,64,64);
                    serverTank = new Tank(new Vector2(10.8f,5.2f),new Vector2(0,0),0);
                    clientTank= new Tank(new Vector2(2,2),new Vector2(0,0),0);
                }
                    //******************************SERVER GЕNERATE ASEROIDS*********************//
                if(BluetoothConnect.SERVER){
                    Random random = new Random();


                    for(int i=0;i<ASTEROIDS_NUMBER;i++){
                        float  cicleD=0.8f;

                        asteroids.add(new Asteroid(new Vector2(random.nextFloat()*smallWindow.width,
                                random.nextFloat()*smallWindow.height),
                                new Vector2(-0.01f+0.02f* random.nextFloat(),-0.01f+0.02f* random.nextFloat()),random.nextFloat()*360));

                    }
                    random=null;
                }

                bulletRegion= new TextureRegion(atlas,128,38,32,32);
                asteroidRegion = new TextureRegion(atlas,0,64,64,64);
                spriteBatcher = new SpriteBatcher(gl,500);

                gl.glViewport(0, 0, getWidth(), getHeight());
                gl.glClearColor(0, 0.5f,1, 1);
                gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
                gl.glMatrixMode(GL10.GL_PROJECTION);
                gl.glLoadIdentity();
                gl.glOrthof(0, FRUSTUM_WIDTH, 0,FRUSTUM_HEIGHT, 1, -1);
                gl.glEnable(GL10.GL_BLEND);
                gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
                gl.glEnable(GL10.GL_TEXTURE_2D);

                gl.glMatrixMode(GL10.GL_MODELVIEW);
                dt= System.currentTimeMillis();
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {

            }

            @Override
            public void onDrawFrame(GL10 gl) {

                startTime= System.currentTimeMillis();
                fps++;
                if(System.currentTimeMillis()-dt>=1000){
                   Log.d("FPS ",BluetoothConnect.SERVER+" "+fps);
                    fps=0;
                    dt= System.currentTimeMillis();
                }

               // serverTank.position.x+=serverTank.velociti.x;
                //serverTank.position.y+=serverTank.velociti.y;


                BluetoothConnect.WRITE_ARREY[0]=serverTank.position.x;
                BluetoothConnect.WRITE_ARREY[1]=serverTank.position.y;
                BluetoothConnect.WRITE_ARREY[2]=serverTank.angle;



                int i =6;
                for(Asteroid asteroid1 : asteroids){
                   BluetoothConnect.WRITE_ARREY[i++]=asteroid1.position.x;
                  //  Log.d("ASTEROID_1",i+" "+asteroid1.position.x);
                   BluetoothConnect.WRITE_ARREY[i++]=asteroid1.position.y;
                    //Log.d("ASTEROID_2",i+" "+asteroid1.position.y);
                   BluetoothConnect.WRITE_ARREY[i++]=asteroid1.angle;
                    //Log.d("ASTEROID_3",i+" "+asteroid1.angle);

                }
                activity.myBluetoothConnect.write();
                BluetoothConnect.WRITE_ARREY[3]=-100;
                BluetoothConnect.WRITE_ARREY[4]=-100;

                clientTank.position.x=BluetoothConnect.READ_ARREY[0];
                clientTank.position.y=BluetoothConnect.READ_ARREY[1];
                clientTank.angle=BluetoothConnect.READ_ARREY[2];

                if(BluetoothConnect.READ_ARREY[3]!=-100&&BluetoothConnect.READ_ARREY[4]!=-100){
                //    Log.v("ARRAY", "" + BluetoothConnect.READ_ARREY[3] + " " + BluetoothConnect.READ_ARREY[4]);
                    bullets.add(new Bullet(new Vector2(BluetoothConnect.READ_ARREY[0],BluetoothConnect.READ_ARREY[1]),
                            new Vector2(BluetoothConnect.READ_ARREY[3],BluetoothConnect.READ_ARREY[4]),BluetoothConnect.READ_ARREY[5]));
//                    clientTankBullet.position.x=BluetoothConnect.READ_ARREY[3];
//                    clientTankBullet.position.y=BluetoothConnect.READ_ARREY[4];
//                    clientTankBullet.angle=BluetoothConnect.READ_ARREY[5];
                  //  Log.v("ARRAY 1", "" + BluetoothConnect.READ_ARREY[3] + " " + BluetoothConnect.READ_ARREY[4]);
                   // BluetoothConnect.READ_ARREY[3]=-100;
                    //BluetoothConnect.READ_ARREY[4]=-100;

                   }
           //     Log.v("ARRAY LENGHT", "" + bullets.size() + " " + BluetoothConnect.READ_ARREY[3]);
               // clientTankBullet.position.x=BluetoothConnect.READ_ARREY[3];
               // clientTankBullet.position.y=BluetoothConnect.READ_ARREY[4];
                //clientTankBullet.angle=BluetoothConnect.READ_ARREY[5];




                gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

                //DRAW BEGIN
                spriteBatcher.beginBatch(atlas);
                if(LittleWindow){
                spriteBatcher.drawSprite(greenSquare.x,greenSquare.y,SCLALE,SCLALE,joistickRegion);
                }else{
                    spriteBatcher.drawSprite(greenSquare.x-3.6f,greenSquare.y-2.4f,SCLALE,SCLALE,joistickRegion);
                }
                serverTank.update();
                spriteBatcher.drawSprite(serverTank.position.x,serverTank.position.y,1,1,serverTank.angle+180,serverTankRegion);

                spriteBatcher.drawSprite(clientTank.position.x,clientTank.position.y,1,1,clientTank.angle+180,clientTankRegion);


                if(!bullets.isEmpty())
                {

                    for(Bullet bull:bullets)
                        {
                            bull.update();
                            spriteBatcher.drawSprite(bull.position.x,bull.position.y,0.2f,0.2f,bull.angle+180,bulletRegion);
                            if(!ColisionTests.pointInRectangle(smallWindow,bull.position)){

                            deleteBullets.add(bull);
                        }

                    }
                }
              //  spriteBatcher.drawSprite(tankBullet.position.x,tankBullet.position.y,0.2f,0.2f,tankBullet.angle,bulletRegion);
              //  spriteBatcher.drawSprite(asteroid.position.x,asteroid.position.y,1,1,asteroid.angle,asteroidRegion);
                if(BluetoothConnect.SERVER){
                    for(Asteroid asteroid1:asteroids){
                        asteroid1.update();
//                        for(Asteroid asteroid2:asteroids){
//                            if(!asteroid1.equals(asteroid2)&&ColisionTests.overlapCiCrcles(asteroid1.position,asteroid1.radius,asteroid2.position,asteroid2.radius)){
//                                    //((asteroid1.position.x-asteroid2.position.x)*(asteroid1.position.x-asteroid2.position.x)+
//                                    //(asteroid1.position.y-asteroid2.position.y)+(asteroid1.position.y-asteroid2.position.y))<(asteroid1.radius+asteroid2.radius)*(asteroid1.radius+asteroid2.radius));
//                            Log.d("Overlap",asteroid1+"  "+asteroid2);
//                           asteroid1.velociti.x*=-1;
//                           asteroid1.velociti.y*=-1;
//                                asteroid2.velociti.x*=-1;
//                                asteroid2.velociti.y*=-1;
//
                            //}
                        //}
                        spriteBatcher.drawSprite(asteroid1.position.x,asteroid1.position.y,1,1
                                ,asteroid1.angle,asteroidRegion);

                    }
                   // Log.d("WRITE_ARRAY", "" + Arrays.toString(BluetoothConnect.WRITE_ARREY));
                  //  Log.d("READ_ARREY", "" + Arrays.toString(BluetoothConnect.READ_ARREY));
                }else {
                   // Log.d("WRITE_ARRAY", "" + Arrays.toString(BluetoothConnect.WRITE_ARREY));
                 //   Log.d("READ_ARREY", "" + Arrays.toString(BluetoothConnect.READ_ARREY));
                    if(BluetoothConnect.READ_ARREY[6]!=-100){
                        spriteBatcher.drawSprite(BluetoothConnect.READ_ARREY[6],BluetoothConnect.READ_ARREY[7],1,1
                                ,BluetoothConnect.READ_ARREY[8],asteroidRegion);
                        int j=9;

                        while(BluetoothConnect.READ_ARREY[j]!=-100){
                            spriteBatcher.drawSprite(BluetoothConnect.READ_ARREY[j++],BluetoothConnect.READ_ARREY[j++],1,1
                                    ,BluetoothConnect.READ_ARREY[j++],asteroidRegion);

                         //   Log.d("DRAW ASTEROID", j+ "");
                        }
                    }
                }

                spriteBatcher.endBatch();






//                if(bullets.size()>15){
//                    bullets.clear();
//                }
                if(!deleteBullets.isEmpty()&&!bullets.isEmpty()){

                    bullets.removeAll(deleteBullets);
                    deleteBullets.clear();
                //    Log.d("BULLETS", bullets.size() + "");
                }
//
//                cameraChange(gl);
            //}
                endTime= System.currentTimeMillis()-startTime;
                if(endTime<40){
                try {
                    Thread.sleep(40 - endTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                }
            }
//            private void cameraChange(GL10 gl){
//                if(SimpleBluetoothTest.SERVER){
//                    if(LittleWindow&&!ColisionTests.pointInRectangle(smallWindow,serverTank.position)){
//                        gl.glViewport(0, 0, getWidth(), getHeight());
//                        gl.glMatrixMode(GL10.GL_PROJECTION);
//                        gl.glLoadIdentity();
//                        gl.glOrthof(-6.4f,19.2f,-3.6f,10.8f, 1, -1);
//                        gl.glMatrixMode(GL10.GL_MODELVIEW);
//                        gl.glLoadIdentity();
//                        LittleWindow=false;
//                        jScale=2;
//                        greenSquare.x-=2;
//                        greenSquare.y-=2;
//
//
//
//
//                        // centre.set(0.4f,-2.4f);
//                    }else if(!LittleWindow&&ColisionTests.pointInRectangle(smallWindow,serverTank.position)){
//                        gl.glViewport(0, 0, getWidth(), getHeight());
//                        gl.glMatrixMode(GL10.GL_PROJECTION);
//                        gl.glLoadIdentity();
//                        gl.glOrthof(0,12.8f,0,7.2f, 1, -1);
//                        gl.glMatrixMode(GL10.GL_MODELVIEW);
//                        gl.glLoadIdentity();
//                        LittleWindow=true;
//                        SCLALE=1;
//
//
//                        // centre.set(2,2);
//                    }
//                }
//                //Log.d("SMALL",clientTank.position.x+" "+clientTank.position.y+" "+SimpleBluetoothTest.SERVER);
//                if(!SimpleBluetoothTest.SERVER){
//                    //Log.d("SMALL",clientTank.position.x+" "+clientTank.position.y);
//                    if(LittleWindow&&!ColisionTests.pointInRectangle(smallWindow,serverTank .position)){
//                        //Log.d("SMALL",clientTank.position.x+" "+clientTank.position.y);
//                        gl.glViewport(0, 0, getWidth(), getHeight());
//                        gl.glMatrixMode(GL10.GL_PROJECTION);
//                        gl.glLoadIdentity();
//                        gl.glOrthof(-6.4f,19.2f,-3.6f,10.8f, 1, -1);
//                        gl.glMatrixMode(GL10.GL_MODELVIEW);
//                        gl.glLoadIdentity();
//                        LittleWindow=false;
//                        SCLALE=2;
//
//                    }else if(!LittleWindow&&ColisionTests.pointInRectangle(smallWindow,serverTank.position)){
//                        // Log.d("BIG",clientTank.position.x+" "+clientTank.position.y);
//                        gl.glViewport(0, 0, getWidth(), getHeight());
//                        gl.glMatrixMode(GL10.GL_PROJECTION);
//                        gl.glLoadIdentity();
//                        gl.glOrthof(0,12.8f,0,7.2f, 1, -1);
//                        gl.glMatrixMode(GL10.GL_MODELVIEW);
//                        gl.glLoadIdentity();
//                        LittleWindow=true;
//                        SCLALE=1;
//                    }
//                }
//            }
        });
    this.setOnTouchListener(new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            float radians=0;
            // Log.v("TOUCH"," "+event.getX()+" "+event.getY()+" "+event.getActionMasked());
            int actionMask = event.getActionMasked();
            // индекс касания
           // int pointerIndex = event.getActionIndex();
            // число касаний
           // int pointerCount = event.getPointerCount();

            switch (actionMask) {
                case MotionEvent.ACTION_DOWN: // первое касание

                    touchPos.x=(event.getX()/(float)getWidth())*FRUSTUM_WIDTH;
                    touchPos.y=(1-event.getY()/(float)getHeight())*FRUSTUM_HEIGHT;
                    if(ColisionTests.pointInCircle(circle, touchPos))touchevent=true;
                  //  Log.v("DIST",""+touchPos.x+" "+touchPos.y+" "+pointerIndex);
//
                    break;
                case MotionEvent.ACTION_POINTER_DOWN: // последующие касания
                    //SHOOTING
                    if(System.currentTimeMillis()-lastShoot>COLDAUN){
                     radians=angle*Vector2.TO_RADIANS;
                 float  A= (FloatMath.cos(radians)* Bullet.ownSpeed);
//                    greenSquare.x=centre.y+ a;
                  float C= (FloatMath.sin(radians)* Bullet.ownSpeed);
//                    greenSquare.y=centre.x+ c;
                        addBullet(new Bullet(serverTank.position.cpy(),new Vector2(A,C),angle));
                 //  bullets.add();
                    BluetoothConnect.WRITE_ARREY[3]=A;
                    BluetoothConnect.WRITE_ARREY[4]=C;
                    BluetoothConnect.WRITE_ARREY[5]=angle;
                    Log.v("A   C  ", A + " " + C);
                        lastShoot=System.currentTimeMillis();
                    }
                    //bulletVelociti.x+=(touchPos.x-centre.x)*0.01;
                    //bulletVelociti.y+=(touchPos.y-centre.y)*0.01;
//                    tankBullet.position.set(serverTank.position);
//                    tankBullet.velociti.set((float) (FloatMath.cos(radians)*Bullet.firstSpeed),(float) (FloatMath.sin(radians)*Bullet.firstSpeed));
//                    tankBullet.angle=angle;
                   // Log.d("TOUCH ",serverTank.position.x+" "+serverTank.position.y+" "+tankBullet.position.x+" "+tankBullet.position.y);
                    //touchevent=false;

                    break;

                case MotionEvent.ACTION_UP: // прерывание последнего касания
                    greenSquare.x=centre.x;
                    greenSquare.y=centre.y;
                    serverTank.position.set(3,3);
                    serverTank.velociti.set(0,0);
                    touchevent=false;
                    break;
                case MotionEvent.ACTION_POINTER_UP: // прерывания касаний

                    break;

                case MotionEvent.ACTION_MOVE: // движение
                    touchPos.x=(event.getX()/(float)getWidth())*FRUSTUM_WIDTH;
                    touchPos.y=(1-event.getY()/(float)getHeight())*FRUSTUM_HEIGHT;
                    //Log.d(" RADIUS",""+((touchPos.x-centre.x)*(touchPos.x-centre.x)+(touchPos.y-centre.y)*(touchPos.y-centre.y))+" "+((greenSquare.x-centre.x)*(greenSquare.x-centre.x)+(greenSquare.y-centre.y)*(greenSquare.y-centre.y))+" CENTR "+centre.x+" "+centre.y+""+event.getActionIndex()+" "+event.getPointerId(0));
                    //Log.d("DIST",touchevent+" "+angle+" "+greenSquare.x+" "+greenSquare.y);
                    float a,c;

                            if(event.getPointerId(0)==0){
                                //angle=touchPos.sub(centre).angle();
                                if(touchevent&&((touchPos.x-centre.x)*(touchPos.x-centre.x)+(touchPos.y-centre.y)*(touchPos.y-centre.y))<=SCLALE*2.56){

                                    greenSquare.x=touchPos.x;
                                    greenSquare.y=touchPos.y;
                                    serverTank.velociti.x= (float) ((touchPos.x-centre.x)*0.1/SCLALE);
                                    serverTank.velociti.y= (float) ((touchPos.y-centre.y)*0.1/SCLALE);
                                    angle=touchPos.sub(centre).angle();
                                    serverTank.angle=angle;
                                }else if(touchevent){

                                    angle=touchPos.sub(centre).angle();
                                    serverTank.angle=angle;
                                    // greenSquare=centre;

                                     radians=angle*Vector2.TO_RADIANS;
                                    a=(float) (FloatMath.cos(radians)*SCLALE*1.6);
                                    greenSquare.x=centre.y+ a;
                                    c=(float) (FloatMath.sin(radians)*SCLALE*1.6);
                                    greenSquare.y=centre.x+ c;
                                    serverTank.velociti.set((float) (a*0.1/SCLALE),(float) (c*0.1/SCLALE));

                                }

                    }



                    break;
            }
            // Log.v("DIST",""+touchPos.x+" "+touchPos.y+" "+pointerIndex+" "+touchevent+" "+event.getPointerId(0));
            return true;
        }
    });
    }




}
