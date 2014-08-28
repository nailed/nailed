/*
 * Nailed, a Minecraft PvP server framework
 * Copyright (C) jk-5 <http://github.com/jk-5/>
 * Copyright (C) Nailed team and contributors <http://github.com/nailed/>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the MIT License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the MIT License along with
 * this program. If not, see <http://opensource.org/licenses/MIT/>.
 */

package jk_5.nailed.api.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TranslatableComponent extends BaseComponent {

    private final ResourceBundle locales = ResourceBundle.getBundle("assets/minecraft/lang/en_US");
    private final Pattern format = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");

    /**
     * The key into the Minecraft locale files to use for the translation. The
     * text depends on the client's locale setting. The console is always en_US
     */
    private String translate;
    /**
     * The components to substitute into the translation
     */
    private List<BaseComponent> with;

    public TranslatableComponent() {
    }

    /**
     * Creates a translatable component with the passed substitutions
     *
     * @param translate the translation key
     * @param with      the {@link java.lang.String}s and
     *                  {@link BaseComponent}s to use into the
     *                  translation
     * @see #translate
     * @see #setWith(java.util.List)
     */
    public TranslatableComponent(String translate, Object... with) {
        setTranslate(translate);
        List<BaseComponent> temp = new ArrayList<BaseComponent>();
        for(Object w : with){
            if(w instanceof String){
                temp.add(new TextComponent((String) w));
            }else{
                temp.add((BaseComponent) w);
            }
        }
        setWith(temp);
    }

    /**
     * Sets the translation substitutions to be used in this component. Removes
     * any previously set substitutions
     *
     * @param components the components to substitute
     */
    public void setWith(List<BaseComponent> components) {
        for(BaseComponent component : components){
            component.parent = this;
        }
        with = components;
    }

    /**
     * Adds a text substitution to the component. The text will inherit this
     * component's formatting
     *
     * @param text the text to substitute
     */
    public void addWith(String text) {
        addWith(new TextComponent(text));
    }

    /**
     * Adds a component substitution to the component. The text will inherit
     * this component's formatting
     *
     * @param component the component to substitute
     */
    public void addWith(BaseComponent component) {
        if(with == null){
            with = new ArrayList<BaseComponent>();
        }
        component.parent = this;
        with.add(component);
    }

    @Override
    protected void toPlainText(StringBuilder builder) {
        try{
            String trans = locales.getString(translate);
            Matcher matcher = format.matcher(trans);
            int position = 0;
            int i = 0;
            while(matcher.find(position)){
                int pos = matcher.start();
                if(pos != position){
                    builder.append(trans.substring(position, pos));
                }
                position = matcher.end();

                String formatCode = matcher.group(2);
                switch(formatCode.charAt(0)){
                    case 's':
                    case 'd':
                        String withIndex = matcher.group(1);
                        with.get(withIndex != null ? Integer.parseInt(withIndex) - 1 : i++).toPlainText(builder);
                        break;
                    case '%':
                        builder.append('%');
                        break;
                }
            }
            if(trans.length() != position){
                builder.append(trans.substring(position, trans.length()));
            }
        }catch(MissingResourceException e){
            builder.append(translate);
        }

        super.toPlainText(builder);
    }

    @Override
    protected void toLegacyText(StringBuilder builder) {
        try{
            String trans = locales.getString(translate);
            Matcher matcher = format.matcher(trans);
            int position = 0;
            int i = 0;
            while(matcher.find(position)){
                int pos = matcher.start();
                if(pos != position){
                    addFormat(builder);
                    builder.append(trans.substring(position, pos));
                }
                position = matcher.end();

                String formatCode = matcher.group(2);
                switch(formatCode.charAt(0)){
                    case 's':
                    case 'd':
                        String withIndex = matcher.group(1);
                        with.get(withIndex != null ? Integer.parseInt(withIndex) - 1 : i++).toLegacyText(builder);
                        break;
                    case '%':
                        addFormat(builder);
                        builder.append('%');
                        break;
                }
            }
            if(trans.length() != position){
                addFormat(builder);
                builder.append(trans.substring(position, trans.length()));
            }
        }catch(MissingResourceException e){
            addFormat(builder);
            builder.append(translate);
        }
        super.toLegacyText(builder);
    }

    private void addFormat(StringBuilder builder) {
        builder.append(getColor());
        if(isBold()){
            builder.append(ChatColor.BOLD);
        }
        if(isItalic()){
            builder.append(ChatColor.ITALIC);
        }
        if(isUnderlined()){
            builder.append(ChatColor.UNDERLINE);
        }
        if(isStrikethrough()){
            builder.append(ChatColor.STRIKETHROUGH);
        }
        if(isObfuscated()){
            builder.append(ChatColor.MAGIC);
        }
    }

    public Pattern getFormat() {
        return format;
    }

    public String getTranslate() {
        return translate;
    }

    public void setTranslate(String translate) {
        this.translate = translate;
    }

    public List<BaseComponent> getWith() {
        return with;
    }
}
