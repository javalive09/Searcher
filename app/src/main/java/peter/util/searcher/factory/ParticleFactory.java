package peter.util.searcher.factory;

import android.graphics.Bitmap;
import android.graphics.Rect;

import peter.util.searcher.particle.Particle;


public abstract class ParticleFactory {
    public abstract Particle[][] generateParticles(Bitmap bitmap, Rect bound);
}
