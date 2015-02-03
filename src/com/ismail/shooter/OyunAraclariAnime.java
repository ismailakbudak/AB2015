package com.ismail.shooter;

import org.anddev.andengine.engine.handler.physics.PhysicsHandler;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;

import android.content.Context;

public class OyunAraclariAnime {
	public int oTextureWidth, oTextureHeight;
	public Texture oTexture;
	public TiledTextureRegion oTextureRegion;
	public AnimatedSprite oSprite;
	public PhysicsHandler oPhy;
	
	public OyunAraclariAnime(int oTextureWidth,int oTextureHeight, TextureOptions oTextureOptions, Context oContext, String oAssetPath, int oTexturePositionX, int oTexturePositionY, float oSpritePositionX, float oSpritePositionY)
	{
		this.oTextureWidth = oTextureWidth;
		this.oTextureHeight = oTextureHeight;
		oTexture = new Texture(oTextureWidth, oTextureHeight, oTextureOptions);
		oTextureRegion = TextureRegionFactory.createTiledFromAsset(oTexture,
				oContext, oAssetPath, oTexturePositionX, oTexturePositionY, 8,
				1);
		// createFromAsset(oTexture, oContext, oAssetPath, oTexturePositionX,
		// oTexturePositionY);
		oSprite = new AnimatedSprite(oSpritePositionX, oSpritePositionY,
				oTextureRegion);
		oPhy = new PhysicsHandler(oSprite);
		oSprite.registerUpdateHandler(oPhy);
	}

	public OyunAraclariAnime() {
		
	}
}
