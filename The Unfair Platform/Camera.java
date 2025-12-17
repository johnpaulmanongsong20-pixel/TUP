public class Camera {
    double x, y, w, h, tx, ty;
    public Camera(double x, double y, double w, double h) { this.x=x; this.y=y; this.w=w; this.h=h; }

    void updateTarget(Player p) { tx = p.x + p.w/2 - w/2; ty = p.y + p.h/2 - h/2; }

    void lerpToTarget(double s) {
        x += (tx - x)*s;
        y += (ty - y)*s;
        if(x<0)x=0;
        if(y<0)y=0;
    }
}