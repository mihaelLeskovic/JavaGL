package simulator.physics.hitboxes;

public interface VisitableHitbox {
    void accept(HitboxVisitor visitor);
}
