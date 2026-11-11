package legends.entity.template;

import java.util.*;

// What each class is allowed to equip
public final class EquipmentRuleTto {
    private final String heroClass;
    private final List<String> weapons;
    private final List<String> armor;
    private final boolean dualWield;
    private final String starterWeapon;

    public EquipmentRuleTto(String heroClass, List<String> weapons, List<String> armor,
            boolean dualWield, String starterWeapon) {
        this.heroClass = heroClass;
        this.weapons = Collections.unmodifiableList(new ArrayList<String>(weapons));
        this.armor = Collections.unmodifiableList(new ArrayList<String>(armor));
        this.dualWield = dualWield;
        this.starterWeapon = starterWeapon;
    }

    public String getHeroClass() {
        return heroClass;
    }

    public List<String> getWeapons() {
        return weapons;
    }

    public List<String> getArmor() {
        return armor;
    }

    public boolean canDualWield() {
        return dualWield;
    }

    public String getStarterWeapon() {
        return starterWeapon;
    }
}
