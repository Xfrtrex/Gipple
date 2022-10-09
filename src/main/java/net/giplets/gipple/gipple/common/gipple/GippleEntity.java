package net.giplets.gipple.gipple.common.gipple;

import net.giplets.gipple.gipple.Gipple;
import net.giplets.gipple.gipple.common.mega_gipple.MegaGippleEntity;
import net.giplets.gipple.gipple.init.GippleTags;
import net.giplets.gipple.gipple.init.GippleSoundEvents;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Flutterer;
import net.minecraft.entity.ai.AboveGroundTargeting;
import net.minecraft.entity.ai.NoPenaltySolidTargeting;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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


public class GippleEntity  extends PathAwareEntity implements IAnimatable, IAnimationTickable, Flutterer {
    private static final TrackedData<Boolean> BLOATED =  DataTracker.registerData(GippleEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private AnimationFactory factory = new AnimationFactory(this);
    private static final Ingredient BREEDING_INGREDIENT = Ingredient.fromTag(GippleTags.GIPPLE_FOOD);



    public GippleEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
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
        this.goalSelector.add(1, new GippleEntity.GippleWanderAroundGoal());
        this.goalSelector.add(1, new FlyGoal(this, 2));
        this.goalSelector.add(1, new AvoidSunlightGoal(this));
        this.goalSelector.add(4, new TemptGoal(this, 0.5D, BREEDING_INGREDIENT, false));
    }

    public static DefaultAttributeContainer.Builder createGippleAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 6.0D)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.25D)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.2)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0)
                .add(EntityAttributes.HORSE_JUMP_STRENGTH, 0);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return GippleSoundEvents.ENTITY_GIPPLE_AMBIENT;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return GippleSoundEvents.ENTITY_GIPPLE_AMBIENT;
    }


    /*
            Damage entity that attacks it
         */
    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.getAttacker() != null && !source.isSourceCreativePlayer()){
            this.getAttacker().damage(DamageSource.sting(this), this.random.nextBetween(1, 4) + 1);
        }
        return super.damage(source, amount);
    }


    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (stack.isIn(GippleTags.GIPPLE_FOOD)) {
            if (!this.world.isClient && !this.isBloated()) {
                world.playSound(null, this.getX(), this.getY(), this.getZ(), GippleSoundEvents.ENTITY_GIPPLE_AMBIENT, SoundCategory.AMBIENT, 1.0F, 1.0F);
                for (int i = 0; i < random.nextBetween(3, 5); i++) {
                    world.addParticle(ParticleTypes.SCRAPE, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
                }
                setBloated(true);
                stack.decrement(1);
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.FAIL;

    }



    @Override
    public boolean canBreatheInWater() {
        return true;
    }
    public boolean hurtByWater() {
        return true;
    }

    /*
            Mitosis
        */
    public EntityType<? extends GippleEntity> getType() {
        return (EntityType<? extends GippleEntity>) super.getType();
    }
    public void remove(RemovalReason reason) {
        //Spawn two gipples with a rare chance of 3
        //5% chance to spawn a mega gipple
        if (isBloated()){
            int gippleNumber = random.nextFloat() > 0.9 ? 3 : 2;
            for (int i = 0; i <gippleNumber; i++){
                if (random.nextFloat() < 0.9) {
                    GippleEntity gipples = Gipple.GIPPLE.create(this.world);

                    if (this.isPersistent()) {
                        gipples.setPersistent();
                    }

                    gipples.setCustomName(this.getCustomName());
                    gipples.setAiDisabled(this.isAiDisabled());
                    gipples.refreshPositionAndAngles(this.getX() + (double) i, this.getY() + 0.5D, this.getZ() + (double) i, this.random.nextFloat() * 360.0F, 0.0F);
                    world.spawnEntity(gipples);
                }
                else{
                    MegaGippleEntity gipples = Gipple.MEGA_GIPPLE.create(world);
                    if (this.isPersistent()) {
                        gipples.setPersistent();
                    }
                    gipples.setCustomName(this.getCustomName());
                    gipples.setAiDisabled(this.isAiDisabled());
                    gipples.refreshPositionAndAngles(this.getX() + (double) i, this.getY() + 0.7D, this.getZ() + (double) i, this.random.nextFloat() * 360.0F, 0.0F);
                    world.spawnEntity(gipples);
                }
            }
        }
        super.remove(reason);
    }


    public int getLimitPerChunk() {
        return 8;
    }

    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(BLOATED, false);
    }
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("Bloated", this.isBloated());
    }

    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setBloated(nbt.getBoolean("Bloated"));
    }
    public boolean cannotDespawn() {
        return this.isBloated() || super.cannotDespawn();
    }

    public boolean canImmediatelyDespawn(double distanceSquared) {
        return !this.hasCustomName();
    }
    protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return dimensions.height * 0.75F;
    }


    public boolean isBloated() {
        return this.dataTracker.get(BLOATED);
    }
    public void setBloated(boolean bloated){
        this.dataTracker.set(BLOATED, bloated);
    }



    /*
            Geckolib stuff
         */
    @Override
    public int tickTimer() {
        return this.age;
    }
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.gipple.swim", true));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<GippleEntity>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }




    /*
        Flying code
     */
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }
    @Override
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    public boolean isInAir() {
        return !this.onGround;
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
    }
    class GippleWanderAroundGoal extends Goal {

        GippleWanderAroundGoal() {
            this.setControls(EnumSet.of(Control.MOVE));
        }

        public boolean canStart() {
            return GippleEntity.this.navigation.isIdle() && GippleEntity.this.random.nextInt(10) == 0;
        }

        public boolean shouldContinue() {
            return GippleEntity.this.navigation.isFollowingPath();
        }

        public void start() {
            Vec3d vec3d = this.getRandomLocation();
            if (vec3d != null) {
                GippleEntity.this.navigation.startMovingAlong(GippleEntity.this.navigation.findPathTo(new BlockPos(vec3d), 1), 1.0D);
            }

        }


        @Nullable
        private Vec3d getRandomLocation() {
            Vec3d vec3d2 = GippleEntity.this.getRotationVec(0.0F);
            Vec3d vec3d3 = AboveGroundTargeting.find(GippleEntity.this, 4, 3, vec3d2.x, vec3d2.z, 1.5707964F, 3, 1);
            return vec3d3 != null ? vec3d3 : NoPenaltySolidTargeting.find(GippleEntity.this, 4, 3, 0, vec3d2.x, vec3d2.z, 1.5707963705062866D);
        }
    }
}
