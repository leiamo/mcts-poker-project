% ================
% background knowledge for rule based poker bots
% ================

:- use_module(library(lists)).

evaluate :- do(A), format('~w~n',[A]), flush_output, halt.
evaluate :- format('~n',[]), flush_output, halt.

round(postflop) :- round(flop).
round(postflop) :- round(turn).
round(postflop) :- round(river).

allcards(C) :- handcards(C1), communitycards(C2), append(C1,C2,C).

% ================
% hand evaluation
% ================
pair(Cards) :- pair(Cards,_).
pair(Cards, N) :-
  member(card(N,C1),Cards),
  member(card(N,C2),Cards),
  C1 \== C2.

doublepair(Cards) :- doublepair(Cards, _, _).
doublepair(Cards, N, M) :-
  pair(Cards, N),
  pair(Cards, M),
  N \== M.

threeofakind(Cards) :- threeofakind(Cards, _).
threeofakind(Cards, N) :-
  member(card(N,C1),Cards),
  member(card(N,C2),Cards),
  C1 \== C2,
  member(card(N,C3),Cards),
  C1 \== C3,
  C2 \== C3.

straight(Cards) :- straight(Cards, _).
straight(Cards, Start) :-
  member(Start, [2,3,4,5,6,7,8,9,10]),
  member(card(Start,_),Cards),
  I2 is Start + 1,
  member(card(I2,_),Cards),
  I3 is Start + 2,
  member(card(I3,_),Cards),
  I4 is Start + 3,
  member(card(I4,_),Cards),
  I5 is Start + 4,
  member(card(I5,_),Cards).

flush(Cards) :-
  member(card(N1,C),Cards),
  member(card(N2,C),Cards),
  N2 \== N1,
  member(card(N3,C),Cards),
  N3 \== N2,
  N3 \== N1,
  member(card(N4,C),Cards),
  N4 \== N1,
  N4 \== N2,
  N4 \== N3,
  member(card(N5,C),Cards),
  N5 \== N1,
  N5 \== N2,
  N5 \== N3,
  N5 \== N4.

fullhouse(Cards) :-
  threeofakind(Cards, N),
  pair(Cards, M),
  N \== M.

fourofakind(Cards) :- fourofakind(Cards, _).
fourofakind(Cards, N) :-
  member(card(N,C1),Cards),
  member(card(N,C2),Cards),
  C1 \== C2,
  member(card(N,C3),Cards),
  C1 \== C3,
  C2 \== C3,
  member(card(N,C4),Cards),
  C1 \== C4,
  C2 \== C4,
  C3 \== C4.

straightflush(Cards) :-
  member(Start, [2,3,4,5,6,7,8,9,10]),
  member(card(Start,C),Cards),
  I2 is Start + 1,
  member(card(I2,C),Cards),
  I3 is Start + 2,
  member(card(I3,C),Cards),
  I4 is Start + 3,
  member(card(I4,C),Cards),
  I5 is Start + 4,
  member(card(I5,C),Cards).

handeval(C,straightflush) :- straightflush(C), !.
handeval(C,fourofakind) :- fourofakind(C), !.
handeval(C,fullhouse) :- fullhouse(C), !.
handeval(C,flush) :- flush(C), !.
handeval(C,straight) :- straight(C), !.
handeval(C,threeofakind) :- threeofakind(C), !.
handeval(C,doublepair) :- doublepair(C), !.
handeval(C,pair) :- pair(C), !.
handeval(C,highcard) :- !.
