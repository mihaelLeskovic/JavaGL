package simulator.physics;

public interface VisitableHitbox {
    void accept(HitboxVisitor visitor);
}
