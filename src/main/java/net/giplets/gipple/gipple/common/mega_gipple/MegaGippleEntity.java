package net.giplets.gipple.gipple.common.mega_gipple;

import net.giplets.gipple.gipple.init.GippleSoundEvents;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.AboveGroundTargeting;
import net.minecraft.entity.ai.NoPenaltySolidTargeting;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimationTickable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.EnumSet;
import java.util.UUID;


public class MegaGippleEntity extends PathAwareEntity implements IAnimatable, IAnimationTickable, Flutterer, Angerable {
    private static final UUID ATTACKING_SPEED_BOOST_ID = UUID.fromString("49455A49-7EC5-45BA-B886-3B90B23A1718");
    private AnimationFactory factory = new AnimationFactory(this);
    private int angrySoundDelay;
    private static final UniformIntProvider ANGER_TIME_RANGE = TimeHelper.betweenSeconds(20, 39);
    private static final UniformIntProvider ANGRY_SOUND_DELAY_RANGE = TimeHelper.betweenSeconds(0, 1);
    private int angerTime;
    @Nullable
    private UUID angryAt;
    private static final int field_30524 = 10;
    private static final UniformIntProvider ANGER_PASSING_COOLDOWN_RANGE = TimeHelper.betweenSeconds(4, 6);
    private int angerPassingCooldown;
    private static final EntityAttributeModifier ATTACKING_SPEED_BOOST = new EntityAttributeModifier(ATTACKING_SPEED_BOOST_ID, "Attacking speed boost", 0.05, EntityAttributeModifier.Operation.ADDITION);




    public MegaGippleEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
        this.moveControl = new FlightMoveControl(this, 10, true);
        this.ignoreCameraFrustum = true;
        this.setPathfindingPenalty(PathNodeType.WATER, -1.0F);
        this.setPathfindingPenalty(PathNodeType.DANGER_FIRE, -1.0F);
        this.setPathfindingPenalty(PathNodeType.DAMAGE_FIRE, -1.0F);
        this.setPathfindingPenalty(PathNodeType.WATER_BORDER, 2.0f);
    }

    protected EntityNavigation createNavigation(World world) {
        BirdNavigation birdNavigation = new BirdNavigation(this, world);
        birdNavigation.setCanPathThroughDoors(false);
        birdNavigation.setCanSwim(true);
        birdNavigation.setCanEnterOpenDoors(true);
        return birdNavigation;
    }

    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(0, new EscapeDangerGoal(this, 0.5D));
        this.goalSelector.add(1, new MegaGippleEntity.GippleWanderAroundGoal());
        this.goalSelector.add(1, new FlyGoal(this, 2));
        this.goalSelector.add(1, new AvoidSunlightGoal(this));
        this.targetSelector.add(1, (new RevengeGoal(this, new Class[0])).setGroupRevenge());
        this.targetSelector.add(2, new ActiveTargetGoal(this, AllayEntity.class, true, false));
//        this.targetSelector.add(2, new ActiveTargetGoal(this, PlayerEntity.class, 10, true, false, shouldAngerAt(PlayerEntity.class)));
        this.targetSelector.add(3, new UniversalAngerGoal(this, true));
    }

    public boolean shouldAngerAt(PlayerEntity entity) {
        if (!this.canTarget(entity)) {
            return false;
        } else {
            return entity.getType() == EntityType.PLAYER && this.isUniversallyAngry(entity.world) || entity.getUuid().equals(this.getAngryAt());
        }
    }

    /*
        Suction behavior
     */
    @Override
    public boolean tryAttack(Entity target) {
        if (isNearerThan(target.getPos(), this.getPos(), 1) && (Math.abs(target.getPos().y - this.getPos().y) < 8)) {
            target.setVelocity(0, 2, 0);
        }
        return super.tryAttack(target);
    }

    public boolean isNearerThan(Vec3d targetPos, Vec3d thisPos, double valueItIs){
        return (Math.abs(targetPos.x - thisPos.x) < valueItIs) && (Math.abs(targetPos.z - thisPos.z) < valueItIs);
    }

    public static DefaultAttributeContainer.Builder createMegaGippleAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0D)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.25D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0)
                .add(EntityAttributes.HORSE_JUMP_STRENGTH, 0);
    }

    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        this.writeAngerToNbt(nbt);
    }

    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.readAngerFromNbt(this.world, nbt);
    }



    /*
        Sound stuff
     */
    protected void playStepSound(BlockPos pos, BlockState state) {
        //Leave empty for no step sound
    }
    protected SoundEvent getAmbientSound() {
        return GippleSoundEvents.ENTITY_GIPPLE_AMBIENT;
    }
    protected SoundEvent getHurtSound(DamageSource source) {
        return GippleSoundEvents.ENTITY_GIPPLE_AMBIENT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_GENERIC_DEATH;
    }

    protected float getSoundVolume() {
        return 0.4F;
    }


    /*
        Damage entity that attacks it
     */
    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.getAttacker() != null && !source.isSourceCreativePlayer()){
            this.getAttacker().damage(DamageSource.sting(this), this.random.nextBetween(2, 5) + 2);
        }
        return super.damage(source, amount);
    }


    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    public boolean canBreatheInWater() {
        return true;
    }
    public boolean hurtByWater() {
        return true;
    }


    public EntityType<? extends MegaGippleEntity> getType() {
        return (EntityType<? extends MegaGippleEntity>) super.getType();
    }


    public int getLimitPerChunk() {
        return 3;
    }

    public boolean cannotDespawn() {
        return super.cannotDespawn();
    }

    public boolean canImmediatelyDespawn(double distanceSquared) {
        return !this.hasCustomName();
    }
    protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return dimensions.height * 0.75F;
    }


    @Override
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    protected boolean shouldFollowLeash() {
        return false;
    }

    /*
            Geckolib stuff
        */
    @Override
    public int tickTimer() {
        return this.age;
    }
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.mega_gipple.swim", true));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<MegaGippleEntity>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    @Override
    public boolean isInAir() {
        return !this.onGround;
    }

    /*
        Anger Code
     */

    protected void mobTick() {
        EntityAttributeInstance entityAttributeInstance = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (this.hasAngerTime()) {

        } else if (entityAttributeInstance.hasModifier(ATTACKING_SPEED_BOOST)) {
            entityAttributeInstance.removeModifier(ATTACKING_SPEED_BOOST);
        }

        this.tickAngerLogic((ServerWorld)this.world, true);
        if (this.getTarget() != null) {
            this.tickAngerPassing();
        }

        if (this.hasAngerTime()) {
            this.playerHitTimer = this.age;
        }

        super.mobTick();
    }
    private void angerNearbyZombifiedPiglins() {
        double d = this.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE);
        Box box = Box.from(this.getPos()).expand(d, 10.0, d);
        this.world.getEntitiesByClass(MegaGippleEntity.class, box, EntityPredicates.EXCEPT_SPECTATOR).stream()
                .filter((megaGippleEntity) -> megaGippleEntity != this)
                .filter((megaGippleEntity) -> megaGippleEntity.getTarget() == null)
                .filter((megaGippleEntity) -> !megaGippleEntity.isTeammate(this.getTarget()))
                .forEach((megaGippleEntity) -> megaGippleEntity.setTarget(this.getTarget()));
    }
    private void tickAngerPassing() {
        if (this.angerPassingCooldown > 0) {
            --this.angerPassingCooldown;
        } else {
            if (this.getVisibilityCache().canSee(this.getTarget())) {
                this.angerNearbyZombifiedPiglins();
            }

            this.angerPassingCooldown = ANGER_PASSING_COOLDOWN_RANGE.get(this.random);
        }
    }
    public boolean isAngryAt(PlayerEntity player) {
        return this.shouldAngerAt(player);
    }

    @Override
    public void setAngerTime(int angerTime) {
        this.angerTime = angerTime;
    }
    @Override
    public int getAngerTime() {
        return this.angerTime;
    }

    @Nullable
    @Override
    public UUID getAngryAt() {
        return this.angryAt;
    }

    @Override
    public void setAngryAt(@Nullable UUID angryAt) {

    }

    @Override
    public void chooseRandomAngerTime() {

    }
    public void setTarget(@Nullable LivingEntity target) {
        if (this.getTarget() == null && target != null) {
            this.angrySoundDelay = ANGRY_SOUND_DELAY_RANGE.get(this.random);
            this.angerPassingCooldown = ANGER_PASSING_COOLDOWN_RANGE.get(this.random);
        }

        if (target instanceof PlayerEntity) {
            this.setAttacking((PlayerEntity)target);
        }

        super.setTarget(target);
    }

    /*
        Flying code
     */
    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
    }
    class GippleWanderAroundGoal extends Goal {

        GippleWanderAroundGoal() {
            this.setControls(EnumSet.of(Control.MOVE));
        }

        public boolean canStart() {
            return MegaGippleEntity.this.navigation.isIdle() && MegaGippleEntity.this.random.nextInt(10) == 0;
        }

        public boolean shouldContinue() {
            return MegaGippleEntity.this.navigation.isFollowingPath();
        }

        public void start() {
            Vec3d vec3d = this.getRandomLocation();
            if (vec3d != null) {
                MegaGippleEntity.this.navigation.startMovingAlong(MegaGippleEntity.this.navigation.findPathTo(new BlockPos(vec3d), 1), 1.0D);
            }

        }


        @Nullable
        private Vec3d getRandomLocation() {
            Vec3d vec3d2 = MegaGippleEntity.this.getRotationVec(0.0F);
            Vec3d vec3d3 = AboveGroundTargeting.find(MegaGippleEntity.this, 4, 3, vec3d2.x, vec3d2.z, 1.5707964F, 3, 1);
            return vec3d3 != null ? vec3d3 : NoPenaltySolidTargeting.find(MegaGippleEntity.this, 4, 3, 0, vec3d2.x, vec3d2.z, 1.5707963705062866D);
        }
    }
}
