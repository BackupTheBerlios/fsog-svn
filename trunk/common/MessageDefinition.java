/* -*- Mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 * vim:expandtab:shiftwidth=4:tabstop=4: */


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


/** Protocol message.
 */
public class MessageDefinition{

    private static int nextIdentifier=1;
        
    public final String name;
    public final int identifier;
    public final String comment;
    public final Sender sentBy;
    public final PieceDefinition[] pieceDefinitions;

    public MessageDefinition(final String name,
                             final String comment,
                             final Sender sentBy,
                             final PieceDefinition[] pieceDefinitions)
        throws Exception
    {
        this.name=name;
        if(nextIdentifier>=127)
            throw new Exception("Too many message definitions.");
        this.identifier=nextIdentifier++;
        this.comment=comment;
        this.sentBy=sentBy;
        this.pieceDefinitions=pieceDefinitions;
    }
}

