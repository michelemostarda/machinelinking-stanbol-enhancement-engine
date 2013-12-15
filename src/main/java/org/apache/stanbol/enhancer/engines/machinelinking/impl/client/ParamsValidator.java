package org.apache.stanbol.enhancer.engines.machinelinking.impl.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * Defines all parameters supported by various methods.
 *
 * @author Michele Mostarda (michele@machinelinking.com)
 */
public class ParamsValidator {

    public static final String app_id = "app_id";
    public static final String app_key = "app_key";
    public static final String id = "id";
    public static final String jsonp = "jsonp";
    public static final String text = "text";
    public static final String text1 = "text1";
    public static final String text2 = "text2";
    public static final String lang = "lang";
    public static final String min_weight = "min_weight";
    public static final String disambiguation = "disambiguation";
    public static final String link = "link";
    public static final String form = "form";
    public static final String cross = "cross";
    public static final String category = "category";
    public static final String external = "external";
    public static final String _abstract = "abstract";
    public static final String _class = "class";
    public static final String image = "image";
    public static final String func = "func";
    public static final String compression_ratio = "compression_ratio";
    public static final String include = "include";
    public static final String include_text = "include_text";
    public static final String output_format = "output_format";

    private static final Object REQUIRED = new Object();

    public enum ParamType {
        String {
            @Override
            String validate(Object v) {
                if(!(v instanceof String))
                    throw new IllegalArgumentException("Expected String.");
                return v.toString();
            }
        },
        Float {
            @Override
            String validate(Object v) {
                if(!(v instanceof Float)) throw new IllegalArgumentException("Expected Float.");
                return java.lang.Float.toString((Float)v);
            }
        },
        Bool {
            @Override
            String validate(Object v) {
                if(!(v instanceof Boolean)) throw new IllegalArgumentException("Expected Boolean.");
                return (Boolean) v ? "1" : "0";
            }
        },
        List {
            @Override
            String validate(Object v) {
                return String.validate(v);
            }
        };

        abstract String validate(Object v);
    }

    public static ParamsValidator getInstance() {
        return instance;
    }

    private static final ParamsValidator instance = new ParamsValidator();

    private List<Param> params = new ArrayList<Param>();

    private static String[] toValues(EnumSet enumeration) {
        final String[] values = new String[enumeration.size()];
        int i = 0;
        for(Object elem : enumeration.toArray()) {
            values[i++] = elem.toString();
        }
        return values;
    }

    protected ParamsValidator() {
        // Method null applies to all methods.
        // Default value null means required.
        addParam(null, app_id, ParamType.String, REQUIRED);
        addParam(null, app_key, ParamType.String, REQUIRED);
        addParam(null, id, ParamType.String, null);
        addParam(null, jsonp, ParamType.String, null);

        addParam("lang", text, ParamType.String, REQUIRED);

        addParam("annotate", text, ParamType.String, REQUIRED);
        addParam("annotate", lang, ParamType.String, null);
        addParam("annotate", min_weight, ParamType.Float, null);
        addParam("annotate", disambiguation, ParamType.Bool, true);
        addParam("annotate", link, ParamType.Bool, true);
        addParam("annotate", form, ParamType.Bool, true);
        addParam("annotate", cross, ParamType.Bool, true);
        addParam("annotate", category, ParamType.Bool, true);
        addParam("annotate", external, ParamType.Bool, true);
        addParam("annotate", _abstract, ParamType.Bool, true);
        addParam("annotate", _class, ParamType.Bool, true);
        addParam("annotate", image, ParamType.Bool, true);
        addParam("annotate", include_text, ParamType.Bool, true);
        addParam("annotate", output_format, ParamType.List, "json", "json", "json-ld", "rdfa", "microdata");

        addParam("compare", text1, ParamType.String, REQUIRED);
        addParam("compare", text2, ParamType.String, REQUIRED);
        addParam("compare", func, ParamType.String, toValues(EnumSet.allOf(ComparisonMethod.class)));

        addParam("summarize", text, ParamType.String, REQUIRED);
        addParam("summarize", compression_ratio, ParamType.Float, null);
        addParam("summarize", func, ParamType.String, toValues(EnumSet.allOf(CompressionMethod.class)));
    }

    protected  <T> void addParam(String method, String name, ParamType type, T defaultValue, T... acceptedValues) {
        params.add( new Param<T>(method, name, type, defaultValue, acceptedValues) );
    }

    private class Param <T> {
        private final String method;
        private final String name;
        private final ParamType paramType;
        private final T defaultValue;
        private final T[] acceptedValues;

        Param(String method, String name, ParamType paramType, T defaultValue, T[] acceptedValues) {
            this.method = method;
            this.name = name;
            this.paramType = paramType;
            this.defaultValue = defaultValue;
            this.acceptedValues = acceptedValues;
        }
    }

    public StringBuilder buildRequest(String method, Map<String,?> candidateParams) {
        final StringBuilder sb = new StringBuilder();
        for (Param param : params) {
            if (param.method == null || (method.equals(param.method))) {
                String value = null;
                for (Map.Entry<String, ?> candidateParam : candidateParams.entrySet()) {
                    if (candidateParam.getKey().equals(param.name)) {
                        value = validateAndConvertType(param, candidateParam.getValue());
                    }
                }
                if (value == null) {
                    if(param.defaultValue == REQUIRED)
                        throw new IllegalArgumentException(String.format("Parameter [%s] is required.", param.name));
                    else
                        value = param.defaultValue == null ? null : convertType(param, param.defaultValue);
                }
                if (value != null) {
                    sb.append(param.name).append("=").append(value);
                    sb.append("&");
                }
            }
        }
        return sb;
    }

    private String convertType(Param param, Object value) {
          return param.paramType.validate(value);
    }

    private String validateAndConvertType(Param param, Object value) {
        if(param.defaultValue == null && value == null)
            throw new IllegalArgumentException(
                    String.format("Illegal value for param %s, must be not null.", param.name)
            );
        if(param.paramType == ParamType.List) {
            if(!Arrays.asList(param.acceptedValues).contains(value))
                throw new IllegalArgumentException(
                        String.format(
                                "Invalid value [%s] for param %s: must be in %s",
                                value, param.name, Arrays.asList(param.acceptedValues)
                        )
                );
            else
                return value.toString();
        }
        return convertType(param, value);
    }

}
