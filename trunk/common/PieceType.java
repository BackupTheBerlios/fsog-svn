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


import java.util.*;

/** Messages are composed of pieces. Each piece has some type,
 * so we know how to (de)serialize it. This type is
 * represented by this enum.
 */
public abstract class PieceType{

    /** C++ and Java type names are different, so we need
     * different methods. */
    public abstract String toCppType
        (final List<FlagSetDefinition> flagSetDefinitions);

    public abstract String toJavaType
        (final List<FlagSetDefinition> flagSetDefinitions);

    /** Can't have Vector<byte>, we need Vector<Byte>.*/
    public abstract String toJavaReferenceType
        (final List<FlagSetDefinition> flagSetDefinitions);

    public abstract String toCppConstType
        (final List<FlagSetDefinition> flagSetDefinitions);

    public abstract String toJavaFinalType
        (final List<FlagSetDefinition> flagSetDefinitions);

    /** But we use the same method names for reading and
     * writing messages. */
    public abstract String getAppender
        (final List<FlagSetDefinition> flagSetDefinitions);

    public abstract String getReader
        (final List<FlagSetDefinition> flagSetDefinitions);

    public static PieceType INT8
        = new PieceType(){
                public String toCppType(final List<FlagSetDefinition> flagSetDefinitions){return "int8_t";}
                public String toJavaType(final List<FlagSetDefinition> flagSetDefinitions){return "byte";}
                public String toJavaReferenceType(final List<FlagSetDefinition> flagSetDefinitions){return "Byte";}
                public String toCppConstType(final List<FlagSetDefinition> flagSetDefinitions){return "const int8_t";}
                public String toJavaFinalType(final List<FlagSetDefinition> flagSetDefinitions){return "final byte";}
                public String getAppender(final List<FlagSetDefinition> flagSetDefinitions){
                    return "append1Byte";
                }
                public String getReader(final List<FlagSetDefinition> flagSetDefinitions){
                    return "read1Byte";
                }
            };
    
    public static PieceType INT16
        = new PieceType(){
                public String toCppType(final List<FlagSetDefinition> flagSetDefinitions){return "int16_t";}
                public String toJavaType(final List<FlagSetDefinition> flagSetDefinitions){return "short";}
                public String toJavaReferenceType(final List<FlagSetDefinition> flagSetDefinitions){return "Short";}
                public String toCppConstType(final List<FlagSetDefinition> flagSetDefinitions){return "const int16_t";}
                public String toJavaFinalType(final List<FlagSetDefinition> flagSetDefinitions){return "final short";}
                public String getAppender(final List<FlagSetDefinition> flagSetDefinitions){
                    return "append2Bytes";
                }
                public String getReader(final List<FlagSetDefinition> flagSetDefinitions){
                    return "read2Bytes";
                }
            };
    
    public static PieceType INT32
        = new PieceType(){
                public String toCppType(final List<FlagSetDefinition> flagSetDefinitions){return "int32_t";}
                public String toJavaType(final List<FlagSetDefinition> flagSetDefinitions){return "int";}
                public String toJavaReferenceType(final List<FlagSetDefinition> flagSetDefinitions){return "Integer";}
                public String toCppConstType(final List<FlagSetDefinition> flagSetDefinitions){return "const int32_t";}
                public String toJavaFinalType(final List<FlagSetDefinition> flagSetDefinitions){return "final int";}
                public String getAppender(final List<FlagSetDefinition> flagSetDefinitions){
                    return "append4Bytes";
                }
                public String getReader(final List<FlagSetDefinition> flagSetDefinitions){
                    return "read4Bytes";
                }
            };
    
    public static PieceType INT64
        = new PieceType(){
                public String toCppType(final List<FlagSetDefinition> flagSetDefinitions){return "int64_t";}
                public String toJavaType(final List<FlagSetDefinition> flagSetDefinitions){return "long";}
                public String toJavaReferenceType(final List<FlagSetDefinition> flagSetDefinitions){return "Long";}
                public String toCppConstType(final List<FlagSetDefinition> flagSetDefinitions){return "const int64_t";}
                public String toJavaFinalType(final List<FlagSetDefinition> flagSetDefinitions){return "final long";}
                public String getAppender(final List<FlagSetDefinition> flagSetDefinitions){
                    return "append8Bytes";
                }
                public String getReader(final List<FlagSetDefinition> flagSetDefinitions){
                    return "read8Bytes";
                }
            };
    
    public static PieceType CSTRING
        = new PieceType(){
                public String toCppType(final List<FlagSetDefinition> flagSetDefinitions){return "std::string";}
                public String toJavaType(final List<FlagSetDefinition> flagSetDefinitions){return "String";}
                public String toJavaReferenceType(final List<FlagSetDefinition> flagSetDefinitions){return "String";}
                public String toCppConstType(final List<FlagSetDefinition> flagSetDefinitions){return "const std::string&";}
                public String toJavaFinalType(final List<FlagSetDefinition> flagSetDefinitions){return "final String";}
                public String getAppender(final List<FlagSetDefinition> flagSetDefinitions){
                    return "appendCString";
                }
                public String getReader(final List<FlagSetDefinition> flagSetDefinitions){
                    return "readCString";
                }
            };
    
    public static PieceType BINARY
        = new PieceType(){
                public String toCppType(final List<FlagSetDefinition> flagSetDefinitions){return "std::vector<char>";}
                public String toJavaType(final List<FlagSetDefinition> flagSetDefinitions){return "java.util.Vector<Byte>";}
                public String toJavaReferenceType(final List<FlagSetDefinition> flagSetDefinitions){return "java.util.Vector<Byte>";}
                public String toCppConstType(final List<FlagSetDefinition> flagSetDefinitions){return "const std::vector<char>&";}
                public String toJavaFinalType(final List<FlagSetDefinition> flagSetDefinitions){return "final java.util.Vector<Byte>";}
                public String getAppender(final List<FlagSetDefinition> flagSetDefinitions){
                    return "appendBinary";
                }
                public String getReader(final List<FlagSetDefinition> flagSetDefinitions){
                    return "readBinary";
                }
            };

    public static PieceType VECTOR(final PieceType t){
        return
            new PieceType(){
            public String toCppType(final List<FlagSetDefinition> flagSetDefinitions){return "std::vector<"+t.toCppType(flagSetDefinitions)+" >";}
            public String toJavaType(final List<FlagSetDefinition> flagSetDefinitions){return "java.util.Vector<"+t.toJavaReferenceType(flagSetDefinitions)+">";}
            public String toJavaReferenceType(final List<FlagSetDefinition> flagSetDefinitions){return "java.util.Vector<"+t.toJavaReferenceType(flagSetDefinitions)+">";}
            public String toCppConstType(final List<FlagSetDefinition> flagSetDefinitions){return "const std::vector<"+t.toCppType(flagSetDefinitions)+">&";}
            public String toJavaFinalType(final List<FlagSetDefinition> flagSetDefinitions){return "final java.util.Vector<"+t.toJavaReferenceType(flagSetDefinitions)+">";}
            public String getAppender(final List<FlagSetDefinition> flagSetDefinitions){return "appendVector";}
            public String getReader(final List<FlagSetDefinition> flagSetDefinitions){return "readVector";}
        };
    }
            

    //For representing enum sets:
    private static class FlagSetPieceType extends PieceType{

        private final String flagSetDefinitionName;

        public FlagSetPieceType(final String flagSetDefinitionName){
            this.flagSetDefinitionName=flagSetDefinitionName;
        }

        public String toCppType
            (final List<FlagSetDefinition> flagSetDefinitions)
        {
            for(FlagSetDefinition flagSetDefinition
                    : flagSetDefinitions){
                if(flagSetDefinition.name!=this.flagSetDefinitionName)
                    continue;

                final int size
                    = flagSetDefinition.flagDefinitions.length;
                if(size>0 && size<=8){
                    return "int8_t";
                } else if(size>8 && size<=16){
                    return "int16_t";
                } else if(size>16 && size<=32){
                    return "int32_t";
                } else{
                    throw new UnsupportedOperationException("The flag set "
                                        +flagSetDefinition.name
                                        +" has unsupported size:"
                                        +size);
                }
            }
            throw new UnsupportedOperationException("No such flag set definition:"
                                +flagSetDefinitionName);
        }

        public String toJavaType
            (final List<FlagSetDefinition> flagSetDefinitions)
        {
            for(FlagSetDefinition flagSetDefinition
                    : flagSetDefinitions){
                if(flagSetDefinition.name!=this.flagSetDefinitionName)
                    continue;

                final int size
                    = flagSetDefinition.flagDefinitions.length;
                if(size>0 && size<=8){
                    return "byte";
                } else if(size>8 && size<=16){
                    return "short";
                } else if(size>16 && size<=32){
                    return "int";
                } else{
                    throw new UnsupportedOperationException("The flag set "
                                                 +flagSetDefinition.name
                                                 +" has unsupported size:"
                                                 +size);
                }
            }
            throw new UnsupportedOperationException("No such flag set definition:"
                                +flagSetDefinitionName);
        }

        public String toJavaReferenceType
            (final List<FlagSetDefinition> flagSetDefinitions)
        {
            for(FlagSetDefinition flagSetDefinition
                    : flagSetDefinitions){
                if(flagSetDefinition.name!=this.flagSetDefinitionName)
                    continue;

                final int size
                    = flagSetDefinition.flagDefinitions.length;
                if(size>0 && size<=8){
                    return "Byte";
                } else if(size>8 && size<=16){
                    return "Short";
                } else if(size>16 && size<=32){
                    return "Integer";
                } else{
                    throw new UnsupportedOperationException("The flag set "
                                                 +flagSetDefinition.name
                                                 +" has unsupported size:"
                                                 +size);
                }
            }
            throw new UnsupportedOperationException("No such flag set definition:"
                                +flagSetDefinitionName);
        }

        public String toCppConstType
            (final List<FlagSetDefinition> flagSetDefinitions)
        {
            for(FlagSetDefinition flagSetDefinition
                    : flagSetDefinitions){
                if(flagSetDefinition.name!=this.flagSetDefinitionName)
                    continue;

                final int size
                    = flagSetDefinition.flagDefinitions.length;
                if(size>0 && size<=8){
                    return "const int8_t";
                } else if(size>8 && size<=16){
                    return "const int16_t";
                } else if(size>16 && size<=32){
                    return "const int32_t";
                } else{
                    throw new UnsupportedOperationException("The flag set "
                                                 +flagSetDefinition.name
                                                 +" has unsupported size:"
                                                 +size);
                }
            }
            throw new UnsupportedOperationException("No such flag set definition:"
                                +flagSetDefinitionName);
        }

        public String toJavaFinalType
            (final List<FlagSetDefinition> flagSetDefinitions)
        {
            for(FlagSetDefinition flagSetDefinition
                    : flagSetDefinitions){
                if(flagSetDefinition.name!=this.flagSetDefinitionName)
                    continue;

                final int size
                    = flagSetDefinition.flagDefinitions.length;
                if(size>0 && size<=8){
                    return "final byte";
                } else if(size>8 && size<=16){
                    return "final short";
                } else if(size>16 && size<=32){
                    return "final int";
                } else{
                    throw new UnsupportedOperationException("The flag set "
                                                 +flagSetDefinition.name
                                                 +" has unsupported size:"
                                                 +size);
                }
            }
            throw new UnsupportedOperationException("No such flag set definition:"
                                +flagSetDefinitionName);
        }

        public String getAppender
            (final List<FlagSetDefinition> flagSetDefinitions)
        {
            for(FlagSetDefinition flagSetDefinition
                    : flagSetDefinitions){
                if(flagSetDefinition.name!=this.flagSetDefinitionName)
                    continue;

                final int size
                    = flagSetDefinition.flagDefinitions.length;
                if(size>0 && size<=8){
                    return "append1Byte";
                } else if(size>8 && size<=16){
                    return "append2Bytes";
                } else if(size>16 && size<=24){
                    return "append3Bytes";
                } else if(size>16 && size<=32){
                    return "append4Bytes";
                } else{
                    throw new UnsupportedOperationException("The flag set "
                                                 +flagSetDefinition.name
                                                 +" has unsupported size:"
                                                 +size);
                }
            }
            throw new UnsupportedOperationException("No such flag set definition:"
                                +flagSetDefinitionName);
        }

        public String getReader
            (final List<FlagSetDefinition> flagSetDefinitions)
        {
            for(FlagSetDefinition flagSetDefinition
                    : flagSetDefinitions){
                if(flagSetDefinition.name!=this.flagSetDefinitionName)
                    continue;

                final int size
                    = flagSetDefinition.flagDefinitions.length;
                if(size>0 && size<=8){
                    return "read1Byte";
                } else if(size>8 && size<=16){
                    return "read2Bytes";
                } else if(size>16 && size<=24){
                    return "read3Bytes";
                } else if(size>16 && size<=32){
                    return "read4Bytes";
                } else{
                    throw new UnsupportedOperationException("The flag set "
                                                 +flagSetDefinition.name
                                                 +" has unsupported size:"
                                                 +size);
                }
            }
            throw new UnsupportedOperationException
                ("No such flag set definition:"
                 +flagSetDefinitionName);
        }
    }

    //To be used by the user defining the protocol:
    public static PieceType FLAGSET(final String flagSetDefinitionName){
        return new FlagSetPieceType(flagSetDefinitionName);
    }
}
