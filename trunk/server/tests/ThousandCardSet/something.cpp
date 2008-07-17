/* -*- Mode: C++; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- 
 * vim:expandtab:shiftwidth=2:tabstop=2: */


/*
  FSOG - Free Software Online Games
  Copyright (C) 2007 Bartlomiej Antoni Szymczak

  This file is part of FSOG.

  FSOG is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/

/*
  You can contact the author, Bartlomiej Antoni Szymczak, by:
  - electronic mail: rhywek@gmail.com
  - paper mail:
  Bartlomiej Antoni Szymczak
  Boegesvinget 8, 1. sal
  2740 Skovlunde
  Denmark
*/

const int8_t nds
  = ThousandProtocol::NINE_SHIFT
  + ThousandProtocol::DIAMOND_SHIFT;

const int8_t jds
  = ThousandProtocol::JACK_SHIFT
  + ThousandProtocol::DIAMOND_SHIFT;

const int8_t qds
  = ThousandProtocol::QUEEN_SHIFT
  + ThousandProtocol::DIAMOND_SHIFT;

const int8_t kds
  = ThousandProtocol::KING_SHIFT
  + ThousandProtocol::DIAMOND_SHIFT;

const int8_t tds
  = ThousandProtocol::TEN_SHIFT
  + ThousandProtocol::DIAMOND_SHIFT;

const int8_t ads
  = ThousandProtocol::ACE_SHIFT
  + ThousandProtocol::DIAMOND_SHIFT;

const int8_t nhs
  = ThousandProtocol::NINE_SHIFT
  + ThousandProtocol::HEART_SHIFT;

const int8_t ncs
  = ThousandProtocol::NINE_SHIFT
  + ThousandProtocol::CLUB_SHIFT;

const int8_t nts = ThousandProtocol::NO_TRUMP_SHIFT;
const int8_t cs = ThousandProtocol::CLUB_SHIFT;

//Simple test:
{
  ThousandCardSet qd;
  qd.addShift(qds);

  qd.containsShift(qds) EQ true;
  qd.containsShift(kds) EQ false;
}

//First card played:
{
  ThousandCardSet qd;
  qd.addShift(qds);

  int8_t trump_shift = nts;

  const bool result = qd.removeFirstShift(qds,trump_shift);

  result EQ true;
  qd.containsShift(qds) EQ false;
  trump_shift EQ nts;
}

{
  ThousandCardSet qd;
  qd.addShift(qds);

  int8_t trump_shift = nts;

  const bool result = qd.removeFirstShift(kds,trump_shift);

  result EQ false;
  qd.containsShift(qds) EQ true;
  trump_shift EQ nts;
}

{
  ThousandCardSet qdkd;
  qdkd.addShift(qds);
  qdkd.addShift(kds);

  int8_t trump_shift = nts;

  const bool result = qdkd.removeFirstShift(qds,trump_shift);

  result EQ true;
  qdkd.containsShift(qds) EQ false;
  qdkd.containsShift(kds) EQ true;
  trump_shift EQ ThousandProtocol::DIAMOND_SHIFT;
}

{
  ThousandCardSet qdkd;
  qdkd.addShift(qds);
  qdkd.addShift(kds);

  int8_t trump_shift = nts;

  const bool result = qdkd.removeFirstShift(kds,trump_shift);

  result EQ true;
  qdkd.containsShift(qds) EQ true;
  qdkd.containsShift(kds) EQ false;
  trump_shift EQ ThousandProtocol::DIAMOND_SHIFT;
}

//Tests of playing the second card over the first one:

//The same color played (diamonds):

//KD over JD:
{
  ThousandCardSet qdkd;
  qdkd.addShift(qds);
  qdkd.addShift(kds);

  const int8_t firstShift = jds;
  const int8_t trumpShift = nts;

  const bool result = qdkd.removeSecondShift(firstShift,kds,trumpShift);

  result EQ true;
  qdkd.containsShift(qds) EQ true;
  qdkd.containsShift(kds) EQ false;
  trumpShift EQ nts;
}

//9D over JD (don't have other D):
{
  ThousandCardSet nd;
  nd.addShift(nds);

  const bool result = nd.removeSecondShift(jds,nds,nts);

  result EQ true;
  nd.containsShift(nds) EQ false;
}

//9D over JD (have other D):
{
  ThousandCardSet tcs;
  tcs.addShift(nds);
  tcs.addShift(qds);

  const bool result = tcs.removeSecondShift(jds,nds,nts);

  result EQ false;
  tcs.containsShift(nds) EQ false;
}

//9D over JD (have other D):
{
  ThousandCardSet tcs;
  tcs.addShift(nds);
  tcs.addShift(kds);

  const bool result = tcs.removeSecondShift(jds,nds,nts);

  result EQ false;
  tcs.containsShift(nds) EQ false;
}

//9D over JD (have other D):
{
  ThousandCardSet tcs;
  tcs.addShift(nds);
  tcs.addShift(tds);

  const bool result = tcs.removeSecondShift(jds,nds,nts);

  result EQ false;
  tcs.containsShift(nds) EQ false;
}

//9D over JD (have other D):
{
  ThousandCardSet tcs;
  tcs.addShift(nds);
  tcs.addShift(ads);

  const bool result = tcs.removeSecondShift(jds,nds,nts);

  result EQ false;
  tcs.containsShift(nds) EQ false;
}

//9D over JD (don't have other D):
{
  ThousandCardSet tcs;
  tcs.addShift(nds);
  tcs.addShift(nhs);

  const bool result = tcs.removeSecondShift(jds,nds,nts);

  result EQ true;
  tcs.containsShift(nds) EQ false;
}

//Different color played (no trump):
{
  ThousandCardSet tcs;
  tcs.addShift(nds);
  tcs.addShift(nhs);

  const bool result = tcs.removeSecondShift(jds,nhs,nts);

  result EQ false;
}

//Different color played (no trump):
{
  ThousandCardSet tcs;
  tcs.addShift(nhs);

  const bool result = tcs.removeSecondShift(jds,nhs,nts);

  result EQ true;
  tcs.containsShift(nhs) EQ false;
}

//9H played over JD, but should play C, as it's trump.
{
  ThousandCardSet tcs;
  tcs.addShift(ncs);
  tcs.addShift(nhs);

  const bool result = tcs.removeSecondShift(jds,nhs,cs);

  result EQ false;
}

//9C played over JD, C is trump.
{
  ThousandCardSet tcs;
  tcs.addShift(ncs);
  tcs.addShift(nhs);

  const bool result = tcs.removeSecondShift(jds,ncs,cs);

  result EQ true;
}

//9C played over JD, C is trump. Should play 9D!
{
  ThousandCardSet tcs;
  tcs.addShift(ncs);
  tcs.addShift(nds);

  const bool result = tcs.removeSecondShift(jds,ncs,cs);

  result EQ false;
}
