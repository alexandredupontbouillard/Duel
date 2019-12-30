use_module(library(jpl)).

explore_points(OffSize,DefSize):-
	OffSize>=2*DefSize. /* true if search for defensive */

being_attacked(Time):-
	Time<10.

areaCovered(Radius,Size,MapWidth):-
	2*pi*Radius*Size<0.6*MapWidth.

inGoodHealth(life):-
	life>3.

shotImpact(Probability):-
	Probability>0.1.

/* DECISIONS */

explore(Time,Size,Radius,MapWidth):-
	not(being_attacked(Time)),
	areaCovered(Radius,Size,MapWidth),
	jpl_call('sma.actionsBehaviours.PrologHeightRandomBehavior',executeExplore,[],@(void)).

hunt(Life,Time,OffSize,DefSize,Radius,MapWidth,EnemyInSight):-
	not(being_attacked(Time)),
	not(areaCovered(Radius,OffSize,MapWidth)),
	not(areaCovered(Radius,DefSize,MapWidth));
	inGoodHealth(Life),
	being_attacked(Time),
	not(EnemyInSight),
	jpl_call('sma.actionsBehaviours.LearningProlog',executeHunt,[],@(void)).

toOpenFire(EnemyInSight,P):-
	shotImpact(P),
	EnemyInSight.

attack(EnemyInSight):-
	EnemyInSight,
    jpl_call('sma.actionsBehaviours.LearningProlog',moveAttack,[],@(void)),
	jpl_call('sma.actionsBehaviours.LearningProlog',executeAttack,[],@(void)).


/*retreat(Life,Time):-  */
/*	not(inGoodHealth(Life)), */
/*	being_attacked(Time),  */
/*	jpl_call('sma.actionsBehaviours.PrologBehavior',executeRetreat,[],@(void)). */
