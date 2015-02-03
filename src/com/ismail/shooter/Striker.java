package com.ismail.shooter;

import java.util.Random;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.anddev.andengine.entity.scene.CameraScene;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.font.FontFactory;
import org.anddev.andengine.opengl.texture.BuildableTexture;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.sensor.accelerometer.AccelerometerData;
import org.anddev.andengine.sensor.accelerometer.IAccelerometerListener;
import org.anddev.andengine.ui.activity.BaseGameActivity;

import android.graphics.Color;
import android.view.KeyEvent;

public class Striker extends BaseGameActivity implements IAccelerometerListener
{
	private static final int CAMERA_WIDTH = 800;
    private static final int CAMERA_HEIGHT = 480;
    private Camera camera;
    private Engine engine;
	Scene sahneOyun, sahneAnaMenu, sahnePauseMenu;
	
	BuildableTexture bTex;
	Font font;
	ChangeableText cTexSkor;
	
	TimerHandler timerOyunSuresi;
	Random rnd;
	
	private int skor = 0;
	
	private int balonDiziX = 8, balonDiziY = 3;
	int kalip = 1;
	
	/* Yukardan gelecek balonlar için 3 yol belirledik.
	 * Aþaðýdaki diziler ile de bu yollarýn hangisinden balon
	 * geleceðini belirleyeceðiz. 1'ler balonun var olduðunu belirtirken
	 * 0 balon olmadýðýný belirler
	*/
	byte kalip1[][]={
			{1,0,0},
			{0,0,0},
			{0,1,0},
			{0,0,0},
			{0,0,1},
			{0,0,0},
			{1,0,0},
			{0,0,0}
			};
	byte kalip2[][]={
			{1,0,1},
			{0,0,1},
			{1,0,0},
			{0,0,0},
			{0,0,1},
			{0,0,0},
			{1,0,0},
			{0,0,0}
			};
	byte kalip3[][]={
			{1,0,0},
			{1,0,0},
			{0,1,0},
			{0,0,1},
			{0,0,1},
			{0,0,0},
			{1,0,0},
			{0,1,0}
			};
	byte kalip4[][]={
			{0,0,0},
			{0,1,0},
			{0,0,0},
			{0,0,1},
			{0,0,1},
			{1,0,0},
			{1,0,0},
			{0,1,0}
			};
	byte kalip5[][]={
			{0,0,1},
			{0,0,1},
			{1,0,0},
			{0,0,1},
			{1,0,1},
			{0,0,0},
			{1,0,0},
			{0,1,0}
			};
	
	private boolean oPhyTanimlandiMi = true;

	// Oyun araçlarý tanýmlanýyor. Hýz verebilmemiz için fiziki bir ortam gereklidir.
	// Bu yüzden oyunAraclari sýnýfýnda bir de PhysicsHandler nesnesi bulunmakta. 
	OyunAraclari arka1, arka2;
	OyunAraclariAnime oyuncu;
	OyunAraclari[][] balonlar = new OyunAraclari[balonDiziX][balonDiziY];
	
	// Menü nesneleri tanýmlanýyor.
	private ClsNesne anaMenuArka, anaMenuOyna,
	anaMenuOynaHover, anaMenuCikis, anaMenuCikisHover,
	pauseMenuArka, pauseMenuMenu, pauseMenuRestart, pauseMenuResume;
	
	// Balon patlama animasyonu için nesneler tanýmlanýyor
	Texture texPatlama;
	TiledTextureRegion tilTexRegPatlama;
	AnimatedSprite animSpritePatlama;
	
	// Sahne kontrolleri yanýmlanýyor
	private boolean anaMenuSahnesiMi = true, oyunSahnesiMi = false, pauseMenuSahnesiMi = false;
	
	// Ýðne hýzý tanýmlanýyor
	private float oyuncuHizi = 0;
	
	@Override
	public Engine onLoadEngine() 
	{
		// Camera boyutu, konumu ve çeþitli ayarlamalar ile birlikte motor ayarlarý yapýlýyor.
		camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
	    final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE, new FillResolutionPolicy(), camera);
	    engineOptions.getTouchOptions().setRunOnUpdateThread(true);
	    engine = new Engine(engineOptions);
		
		return engine;
	}

	@Override
	public void onLoadResources() 
	{
		//Ana menü nesneleri oluþturuluyor
		anaMenuArka = new ClsNesne(1024, 512,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA, this,
				"shoot/back.png",
				0, 0);
		anaMenuOyna = new ClsNesne(64, 256,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA, this,
				"shoot/bt_play.png", 0, 0);
		anaMenuOynaHover = new ClsNesne(64, 256,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA, this,
				"shoot/bt_play_hover.png", 0, 0);
		anaMenuCikis = new ClsNesne(64, 128,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA, this,
				"shoot/bt_quit.png", 0, 0);
		anaMenuCikisHover = new ClsNesne(64, 128,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA, this,
				"shoot/bt_quit_hover.png", 0, 0);
				
		// Pause menü nesneleri oluþturuluyor
		pauseMenuArka = new ClsNesne(1024, 512,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA, this,
				"shoot/pauseArkaplan.png", 0, 0);
		pauseMenuRestart = new ClsNesne(128, 128,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA, this,
				"shoot/restart.png", 0, 0);
		pauseMenuResume = new ClsNesne(128, 128,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA, this,
				"shoot/resume.png", 0, 0);
		pauseMenuMenu = new ClsNesne(128, 128,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA, this,
				"shoot/menu.png",
				0, 0);

		// Patlama animasyonu için texture ve tiledTextureRegion nesneleri oluþturuluyor
		texPatlama = new Texture(256, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		tilTexRegPatlama = TextureRegionFactory.createTiledFromAsset(
				texPatlama, this, "shoot/patlama_anim.png", 0, 0, 1, 4);
		
		// Oyun araçlarý oluþturuluyor.
		arka1 = new OyunAraclari(1024, 512,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA, this,
				"shoot/arkaplan.jpg", 0, 0, 800 - 1024, 0);
		arka2 = new OyunAraclari(1024, 512,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA, this,
				"shoot/arkaplan.jpg", 0, 0, 800 - 2048, 0);
		oyuncu = new OyunAraclariAnime(1024, 64,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA, this,
				"shoot/animeShoot.png", 0, 0, 550, 200);
		
		// Balonlar rastgele renklerde oluþturuluyor
		for(int i = 0; i < balonDiziX; i++)
		{
			for(int j = 0; j < balonDiziY; j++)
			{
				balonlar[i][j] = new OyunAraclari(64, 64,
							TextureOptions.BILINEAR_PREMULTIPLYALPHA, this,
							"shoot/mine.png", 0, 0, i * 256 - 1600,
							60 + j * 122);
				engine.getTextureManager().loadTexture(balonlar[i][j].oTexture);
				
			}
		}
		
		//Ekrana text nesnesi yazdýrmak için gerekli BuildableTexture nesnesi oluþturuluyor.
		if(bTex == null)
		{
			bTex = new BuildableTexture(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		}
		else
		{
			bTex = null;
			bTex = new BuildableTexture(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		}
		
		// Ekrana text nesnesi yazdýrmak için gerekli Font nesnesi oluþturuluyor.
		this.font = FontFactory.createStrokeFromAsset(this.bTex, this,
				"shoot/CippHand.ttf", 75, true, Color.WHITE, 2,
				Color.rgb(97, 134, 147));// FontFactory.createFromAsset(bTex,
											// this, "shoot/CippHand.ttf", 25,
											// true, color.black);
		
		// Texture nesneleri, donanýma yüklenmek üzere bir dizide tutuluyor.
		Texture []textures = {texPatlama, bTex, arka1.oTexture, arka2.oTexture, oyuncu.oTexture,
				anaMenuArka.oTexture, anaMenuOyna.oTexture, anaMenuOynaHover.oTexture, anaMenuCikis.oTexture, anaMenuCikisHover.oTexture,
				pauseMenuArka.oTexture, pauseMenuMenu.oTexture, pauseMenuRestart.oTexture, pauseMenuResume.oTexture};
		
		// Texture ve Font nesneleri yükleniyor.
		this.engine.getFontManager().loadFont(font);
		this.engine.getTextureManager().loadTextures(textures);
	}

	@Override
	public Scene onLoadScene() 
	{
		this.engine.registerUpdateHandler(new FPSLogger());
		
		// Sensör etkinleþtiriliyor.
		this.enableAccelerometerSensor(this);
		
		// Sahne nesneleri oluþtutuluyor
		this.sahneOyun = new Scene();
		this.sahneAnaMenu = new Scene();
		this.sahnePauseMenu = new CameraScene(this.camera);
		
		this.cTexSkor = new ChangeableText(20, 240, font, "", 6);
		
		this.cTexSkor.setRotation(-90);
		this.cTexSkor.setText("0");
		
		// Animasyon nesnesi oluþturuluyor.
		this.animSpritePatlama = new AnimatedSprite(0, 0, tilTexRegPatlama);
		
		// Metot isimlerinden hangi metodun hangi iþlemleri yapacaðýný çýkarabilirsiniz
		this.anaMenuNesneleriniOlustur();
		this.pauseMenuNesneleriniOlustur();
		this.arkaplanKontrolleri();
		
		// sahneOyun Sahnesine nesneler çizdiriliyor
		this.sahneOyun.attachChild(arka1.oSprite);
		this.sahneOyun.attachChild(arka2.oSprite);
		this.sahneOyun.attachChild(oyuncu.oSprite);
		this.sahneOyun.attachChild(cTexSkor);
		this.sahneOyun.attachChild(animSpritePatlama);
		
		this.animSpritePatlama.setVisible(false);
		
		// Balonlarýn görünürlükleri yukarýdaki integer dizi kalýplarýna göre ayarlanýyor
		for(int i = 0; i < 8; i++)
		{
			for(int j = 0; j < 3; j++)
			{	
				balonlar[i][j].oPhy.setVelocity(200, 0);
				this.sahneOyun.attachChild(balonlar[i][j].oSprite);
				if(kalip1[i][j] == 1)
				{
					
				}
				else
				{
					balonlar[i][j].oSprite.setVisible(false);
				}
			}
		}
		return sahneAnaMenu;
	}

	@Override
	public void onLoadComplete() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
    public void onAccelerometerChanged(final AccelerometerData pAccelerometerData) 
	{
		oyuncuHizi = pAccelerometerData.getY();
    }
	
	// Arkaplan nesnesinin hareketini ve oyuncunun hýzýný kontrol eden metot
	// amaç, resimlerden biri aþaðýdan ekrandan çýktýðý anda tekrar üste, 
	// yani diðer resmin üstüne yapýþýk bir þekilde hareket ettirmek ve bu döngüyü saðlamak
	private void arkaplanKontrolleri()
	{
		
		this.engine.registerUpdateHandler(new IUpdateHandler() {
			
			@Override
			public void reset() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onUpdate(float pSecondsElapsed) 
			{
				arka1.oPhy.setVelocityX(300f * 1.5f);
				arka2.oPhy.setVelocityX(300f * 1.5f);
				
				// oyuncunun konumunu belirleyen alan
				if(oyuncuHizi > 0)
				{
					if (oyuncu.oSprite.getY() <= 0)
					{
						oyuncu.oPhy.setVelocity(0, oyuncuHizi*40);
					}
 else if (oyuncu.oSprite.getY() > 0
							&& oyuncu.oSprite.getY() <= CAMERA_HEIGHT
									- oyuncu.oTextureHeight)
					{
						oyuncu.oPhy.setVelocity(0, oyuncuHizi*40);
					}
					else
					{
						oyuncu.oPhy.setVelocity(0, 0);
					}
				}
				
				if(oyuncuHizi < 0)
				{
					if (oyuncu.oSprite.getY() > CAMERA_HEIGHT
							- oyuncu.oTextureHeight)
					{
						oyuncu.oPhy.setVelocity(0, oyuncuHizi*40);
					}
 else if (oyuncu.oSprite.getY() > 0
							&& oyuncu.oSprite.getY() <= CAMERA_HEIGHT
									- oyuncu.oTextureHeight)
					{
						oyuncu.oPhy.setVelocity(0, oyuncuHizi*40);
					}
					else
					{
						oyuncu.oPhy.setVelocity(0, 0);
					}
				}
				

				if(arka1.oSprite.getX() >= 800)
				{
					arka1.oSprite.setPosition(arka2.oSprite.getX()-1024, 0);
				}
				if(arka2.oSprite.getX() >= 800)
				{
					arka2.oSprite.setPosition(arka1.oSprite.getX()-1024, 0);
				}
				
				if(balonlar[0][0].oSprite.getX() >= 800)
				{
					kalipDegistir();
				}
				
				for(int i = 0; i < balonDiziX; i++)
				{
					for(int j = 0; j < balonDiziY; j++)
					{	
						// Balon þeklinden dolayý çarpýþmalarý if kontrolü ile ayrýntýlý bir þekilde kontrol etmemiz gerekiyor
						// Aksi taktirde normalde kare olan resmin içinde balon küçük bir alan kaplýyor. 
						// Bu durumda iðne balona çarpmadan patlayabiliyor. Bunu önlemek için if bloklarý ile kendi kontrollerimizi yapýyoruz
						if (balonlar[i][j].oSprite.collidesWith(oyuncu.oSprite)
								&& (oyuncu.oSprite.getY() < balonlar[i][j].oSprite
										.getY()
										+ balonlar[i][j].oSprite.getWidth())
								&& (oyuncu.oSprite.getX() < balonlar[i][j].oSprite
										.getX()
										+ balonlar[i][j].oSprite.getHeight()
										/ 2))
						{
							if(balonlar[i][j].oSprite.isVisible())
							{
								skor++;
								cTexSkor.setText(Integer.toString(skor));
								
								balonlar[i][j].oSprite.setVisible(false);
								animSpritePatlama.setPosition(
										balonlar[i][j].oSprite.getX(),
										balonlar[i][j].oSprite.getY() - 100);
								animSpritePatlama.setVisible(true);
								animSpritePatlama.animate(150);
								
								engine.registerUpdateHandler(timerOyunSuresi = new TimerHandler(0.5f, false, new ITimerCallback() {
									
									@Override
									public void onTimePassed(TimerHandler pTimerHandler) 
									{
										// TODO Auto-generated method stub
										animSpritePatlama.setVisible(false);
										animSpritePatlama.stopAnimation(0);
									}
								}));
							}
							
						}
					}
				}
			}
		});
	}
	
	// Kalýplarýn yani balon yerlerinin deðiþtirilmesini saðlayan metot
	private void kalipDegistir() 
	{
		kalip++;
		for(int i = 0; i < 8; i++)
		{
			for(int j = 0; j < 3; j++)
			{
				if(kalip == 1)
				{
					if(kalip3[i][j] == 1)
					{
						balonlar[i][j].oSprite.setVisible(false);
					}
					if(kalip1[i][j] == 1)
					{
						balonlar[i][j].oSprite.setVisible(true);
						balonlar[i][j].oSprite.setPosition(i*256 - 1600, 60 + j * 122);
					}
				}
				
				if(kalip == 2)
				{
					if(kalip1[i][j] == 1)
					{
						balonlar[i][j].oSprite.setVisible(false);
					}
					if(kalip2[i][j] == 1)
					{
						balonlar[i][j].oSprite.setVisible(true);
						balonlar[i][j].oSprite.setPosition(i*256 - 1600, 60 + j * 122);
					}
				}
				
				if(kalip == 3)
				{
					if(kalip2[i][j] == 1)
					{
						balonlar[i][j].oSprite.setVisible(false);
					}
					if(kalip3[i][j] == 1)
					{
						balonlar[i][j].oSprite.setVisible(true);
						balonlar[i][j].oSprite.setPosition(i*256 - 1600, 60 + j * 122);
					}
				}
				
				if(kalip == 4)
				{
					if(kalip3[i][j] == 1)
					{
						balonlar[i][j].oSprite.setVisible(false);
					}
					if(kalip4[i][j] == 1)
					{
						balonlar[i][j].oSprite.setVisible(true);
						balonlar[i][j].oSprite.setPosition(i*256 - 1600, 60 + j * 122);
					}
				}
				
				if(kalip == 5)
				{
					if(kalip4[i][j] == 1)
					{
						balonlar[i][j].oSprite.setVisible(false);
					}
					if(kalip5[i][j] == 1)
					{
						balonlar[i][j].oSprite.setVisible(true);
						balonlar[i][j].oSprite.setPosition(i*256 - 1600, 60 + j * 122);
					}
					if(i == balonDiziX -1 && j == balonDiziY - 1)
					{
						kalip = 1;
					}
				}
			}
		}		
	}  
	
	private void anaMenuNesneleriniOlustur()
	{
		anaMenuArka.oSprite = new Sprite(0, 0, anaMenuArka.oTextureRegion);
		

		anaMenuOynaHover.oSprite = new Sprite(312, 107, anaMenuOynaHover.oTextureRegion);
		anaMenuOyna.oSprite = new Sprite(312, 107, anaMenuOyna.oTextureRegion)
		{
			@Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
            		float pTouchAreaLocalX, float pTouchAreaLocalY) 
			{
    			if(pSceneTouchEvent.isActionDown())
    			{
    				anaMenuOyna.oSprite.setVisible(false);
    				anaMenuOynaHover.oSprite.setVisible(true);
    			}
    			if(pSceneTouchEvent.isActionUp())
    			{   
    				anaMenuOyna.oSprite.setVisible(true);
    				anaMenuOynaHover.oSprite.setVisible(false);
    				anaMenuSahnesiMi = false;
    				oyunSahnesiMi = true;
    				restartGame();
    				engine.setScene(sahneOyun);
    			}
                return true;
            }
		};
		
		//Hover ve asýl nesne ayný koordinatlarda oluþturuluyor
		anaMenuCikisHover.oSprite = new Sprite(685, 60, anaMenuCikisHover.oTextureRegion);
		anaMenuCikis.oSprite = new Sprite(685, 60, anaMenuCikis.oTextureRegion)
		{
			@Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
                            float pTouchAreaLocalX, float pTouchAreaLocalY) 
			{
    			if(pSceneTouchEvent.isActionDown())
    			{
    				anaMenuCikis.oSprite.setVisible(false);
    				anaMenuCikisHover.oSprite.setVisible(true);
    			}
    			if(pSceneTouchEvent.isActionUp())
    			{   
    				finish();
    	            System.exit(0);
    			}
                return true;
            }
		};
		
		// Hover nesnelerinin görünürlük özelliði false yapýlýyor
		anaMenuOynaHover.oSprite.setVisible(false);
		anaMenuCikisHover.oSprite.setVisible(false);
		
		// sahneMenu nesneleri sahneye çizdiriliyor
		this.sahneAnaMenu.attachChild(anaMenuArka.oSprite);
		this.sahneAnaMenu.attachChild(anaMenuOyna.oSprite);
		this.sahneAnaMenu.attachChild(anaMenuOynaHover.oSprite);
		this.sahneAnaMenu.attachChild(anaMenuCikis.oSprite);
		this.sahneAnaMenu.attachChild(anaMenuCikisHover.oSprite);
		
		// sahneMenu üzerindeki butonlarýn RegisterArea
		// özellikleri tanýmlanýyor 
		// (Hover nesnelerin dokunma  özellikleri hariç)
		this.sahneAnaMenu.registerTouchArea(anaMenuOyna.oSprite);
		this.sahneAnaMenu.registerTouchArea(anaMenuCikis.oSprite);
		
		
	}
	
	// Pause menu nesneleri oluþturuluyor. Yine þeffaf bir sahne olacak pause menu sahnesi de
	private void pauseMenuNesneleriniOlustur()
	{
		pauseMenuArka.oSprite = new Sprite(0, 0, pauseMenuArka.oTextureRegion);
		pauseMenuRestart.oSprite = new Sprite(200, 110, pauseMenuRestart.oTextureRegion)
		{
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) 
			{
				if (pSceneTouchEvent.isActionDown()) 
				{
					
				}
				if (pSceneTouchEvent.isActionUp()) 
				{
					sahneOyun.clearChildScene();
					restartGame();
				}
				return true;
			}
		};
		pauseMenuResume.oSprite = new Sprite(200, 220, pauseMenuResume.oTextureRegion)
		{
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) 
			{
				if (pSceneTouchEvent.isActionDown()) 
				{
					
				}
				if (pSceneTouchEvent.isActionUp()) 
				{
					pauseMenuSahnesiMi = false;
    				oyunSahnesiMi = true;
					sahneOyun.clearChildScene();
					resumeGame();
				}
				return true;
			}
		};
		pauseMenuMenu.oSprite = new Sprite(200, 330, pauseMenuMenu.oTextureRegion)
		{
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) 
			{
				if (pSceneTouchEvent.isActionDown()) 
				{
					
           				
				}
				if (pSceneTouchEvent.isActionUp()) 
				{
    				//restart();
    				anaMenuSahnesiMi = true;
    				oyunSahnesiMi = false;
    				sahneOyun.clearChildScene();
					engine.setScene(sahneAnaMenu);
				}
				return true;
			}
		};
		
		sahnePauseMenu.attachChild(pauseMenuArka.oSprite);
		sahnePauseMenu.attachChild(pauseMenuMenu.oSprite);
		sahnePauseMenu.attachChild(pauseMenuRestart.oSprite);
		sahnePauseMenu.attachChild(pauseMenuResume.oSprite);
		
		sahnePauseMenu.registerTouchArea(pauseMenuMenu.oSprite);
		sahnePauseMenu.registerTouchArea(pauseMenuRestart.oSprite);
		sahnePauseMenu.registerTouchArea(pauseMenuResume.oSprite);
		
		// Þeffaf sahneler(CameraScene) için gerekli iki satýr
		this.sahneAnaMenu.setTouchAreaBindingEnabled(true);
		this.sahnePauseMenu.setBackgroundEnabled(false);
	}
	
	// Oyunun durdurulmasýný saðlayan metot
	private void pauseGame()
	{
		if(oPhyTanimlandiMi)
		{	
			for(int i = 0; i < 8; i++)
			{
				for(int j = 0; j < 3; j++)
				{	
					balonlar[i][j].oSprite.unregisterUpdateHandler(balonlar[i][j].oPhy);
				}
			}
			
			// Oyunu durdurmak için fiziksel olaylarý durdurmak yeterli olacaktýr.
			// Aþaðýdaki ifadelerle tanýmlanmýþ olan update handler nesnesini devre dýþý býrakýyoruz.
			// Oyunu tekrar devam ettirmek için bu ifadeyi tekrar tanýmlýyoruz
			arka1.oSprite.unregisterUpdateHandler(arka1.oPhy);
			arka2.oSprite.unregisterUpdateHandler(arka2.oPhy);
			oyuncu.oSprite.unregisterUpdateHandler(oyuncu.oPhy);
			oPhyTanimlandiMi = false;
		}
	}
	
	private void resumeGame()
	{
		if(!oPhyTanimlandiMi)
		{
			for(int i = 0; i < 8; i++)
			{
				for(int j = 0; j < 3; j++)
				{	
					balonlar[i][j].oSprite.registerUpdateHandler(balonlar[i][j].oPhy);
				}
			}
			
			// updateHandler nesnesi tekrar tanýmlanýyor
			arka1.oSprite.registerUpdateHandler(arka1.oPhy);
			arka2.oSprite.registerUpdateHandler(arka2.oPhy);
			oyuncu.oSprite.registerUpdateHandler(oyuncu.oPhy);
			oPhyTanimlandiMi = true;
		}
	}
	
	// Deðerler sýfýrlanýyor
	private void restartGame()
	{
		pauseMenuSahnesiMi = false;
		oyunSahnesiMi = true;
		kalip = 0;
		skor = 0;
		cTexSkor.setText(Integer.toString(skor));
		oyuncu.oSprite.setPosition(550, 200);
		kalipDegistir();
		resumeGame();
	}
	
	// Fiziksel tuþlarýn kullanýmýný saðlayan metotlar.
	@Override
	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent)
	{
		// Geri Tuþuna basýldýðýnda yapýlacaklar
		if(pKeyCode == KeyEvent.KEYCODE_BACK && pEvent.getAction() == KeyEvent.ACTION_DOWN) 
		{
			if(oyunSahnesiMi)
			{
				pauseMenuSahnesiMi = true;
				oyunSahnesiMi = false;
				pauseGame();
				sahneOyun.setChildScene(sahnePauseMenu);
			}
			else if(pauseMenuSahnesiMi)
			{
				pauseMenuSahnesiMi = false;
				oyunSahnesiMi = true;
				resumeGame();
				sahneOyun.clearChildScene();
			}
			else if(anaMenuSahnesiMi)
			{
				System.exit(0);
			}
            
			return true;
		}
		// Menu tuþuna basýldýðýnda yapýlacaklar
		else if(pKeyCode == KeyEvent.KEYCODE_MENU && pEvent.getAction() == KeyEvent.ACTION_DOWN) 
		{		
			// Bu kýsýmda bir þey yapmaya gerek duymadýk
			// Ama istenilen bir görev buraya yazýlabilir.
			return true;
		}
		else 
		{
			return super.onKeyDown(pKeyCode, pEvent);
		}
	}
}