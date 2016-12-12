package javax.el;
private static class MethodSignature {
    private final String name;
    private final String[] parameterTypeNames;
    public MethodSignature ( final ELContext context, final String methodName, final String className ) throws NoSuchMethodException {
        final int paramIndex = methodName.indexOf ( 40 );
        if ( paramIndex == -1 ) {
            this.name = methodName.trim();
            this.parameterTypeNames = null;
        } else {
            final String returnTypeAndName = methodName.substring ( 0, paramIndex ).trim();
            int wsPos = -1;
            for ( int i = 0; i < returnTypeAndName.length(); ++i ) {
                if ( Character.isWhitespace ( returnTypeAndName.charAt ( i ) ) ) {
                    wsPos = i;
                    break;
                }
            }
            if ( wsPos == -1 ) {
                throw new NoSuchMethodException();
            }
            this.name = returnTypeAndName.substring ( wsPos ).trim();
            String paramString = methodName.substring ( paramIndex ).trim();
            if ( !paramString.endsWith ( ")" ) ) {
                throw new NoSuchMethodException ( Util.message ( context, "elProcessor.defineFunctionInvalidParameterList", paramString, methodName, className ) );
            }
            paramString = paramString.substring ( 1, paramString.length() - 1 ).trim();
            if ( paramString.length() == 0 ) {
                this.parameterTypeNames = ELProcessor.access$000();
            } else {
                this.parameterTypeNames = paramString.split ( "," );
                final ImportHandler importHandler = context.getImportHandler();
                for ( int j = 0; j < this.parameterTypeNames.length; ++j ) {
                    String parameterTypeName = this.parameterTypeNames[j].trim();
                    int dimension = 0;
                    int bracketPos = parameterTypeName.indexOf ( 91 );
                    if ( bracketPos > -1 ) {
                        final String parameterTypeNameOnly = parameterTypeName.substring ( 0, bracketPos ).trim();
                        while ( bracketPos > -1 ) {
                            ++dimension;
                            bracketPos = parameterTypeName.indexOf ( 91, bracketPos + 1 );
                        }
                        parameterTypeName = parameterTypeNameOnly;
                    }
                    boolean varArgs = false;
                    if ( parameterTypeName.endsWith ( "..." ) ) {
                        varArgs = true;
                        dimension = 1;
                        parameterTypeName = parameterTypeName.substring ( 0, parameterTypeName.length() - 3 ).trim();
                    }
                    final boolean isPrimitive = ELProcessor.access$100().contains ( parameterTypeName );
                    if ( isPrimitive && dimension > 0 ) {
                        final String s = parameterTypeName;
                        switch ( s ) {
                        case "boolean": {
                            parameterTypeName = "Z";
                            break;
                        }
                        case "byte": {
                            parameterTypeName = "B";
                            break;
                        }
                        case "char": {
                            parameterTypeName = "C";
                            break;
                        }
                        case "double": {
                            parameterTypeName = "D";
                            break;
                        }
                        case "float": {
                            parameterTypeName = "F";
                            break;
                        }
                        case "int": {
                            parameterTypeName = "I";
                            break;
                        }
                        case "long": {
                            parameterTypeName = "J";
                            break;
                        }
                        case "short": {
                            parameterTypeName = "S";
                            break;
                        }
                        }
                    } else if ( !isPrimitive && !parameterTypeName.contains ( "." ) ) {
                        final Class<?> clazz = importHandler.resolveClass ( parameterTypeName );
                        if ( clazz == null ) {
                            throw new NoSuchMethodException ( Util.message ( context, "elProcessor.defineFunctionInvalidParameterTypeName", this.parameterTypeNames[j], methodName, className ) );
                        }
                        parameterTypeName = clazz.getName();
                    }
                    if ( dimension > 0 ) {
                        final StringBuilder sb = new StringBuilder();
                        for ( int k = 0; k < dimension; ++k ) {
                            sb.append ( '[' );
                        }
                        if ( !isPrimitive ) {
                            sb.append ( 'L' );
                        }
                        sb.append ( parameterTypeName );
                        if ( !isPrimitive ) {
                            sb.append ( ';' );
                        }
                        parameterTypeName = sb.toString();
                    }
                    if ( varArgs ) {
                        parameterTypeName += "...";
                    }
                    this.parameterTypeNames[j] = parameterTypeName;
                }
            }
        }
    }
    public String getName() {
        return this.name;
    }
    public String[] getParamTypeNames() {
        return this.parameterTypeNames;
    }
}
