package im.hira.tweaks.modules.Utilities;

import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradedItem;

import java.util.ArrayList;
import java.util.Optional;

public class BetterGuiMerchant extends MerchantScreen implements AutoTrade {

    private int frames;     //DEBUG
    private ArrayList<Item> wantedTradeItems;

    public BetterGuiMerchant(MerchantScreenHandler handler, PlayerInventory inv, Text title, ArrayList<Item> wantedTrades) {
        super(handler, inv, title);
        wantedTradeItems = wantedTrades;

        System.out.println("wanted items: " + wantedTradeItems);
        frames = 0;
    }

    private TradeOffer[] getTradeRecipes() {
        TradeOfferList trades = handler.getRecipes();
        TradeOffer[] list = new TradeOffer[trades.size()];
        for (int i = 0; i < list.length; i++) {
            list[i] = trades.get(i);
        }
        return list;
    }

    private int getWantedItemIndex(Item item) {
        System.out.println("Getting index for item: " + item);
        TradeOfferList trades = handler.getRecipes();
        for (int i = 0; i < trades.size(); i++) {
            if (trades.get(i).getSellItem().getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void trade(int tradeIndex) {
        System.out.println("trade index: " + tradeIndex);
        TradeOfferList trades = handler.getRecipes();
        System.out.println("wanted length: " + wantedTradeItems.size());
        System.out.println("trades size: " + trades.size());
        System.out.println("sell item: " + trades.get(tradeIndex).getSellItem().getItem());

        TradeOffer recipe = trades.get(tradeIndex);

        int safeguard = 0;
        while (!recipe.isDisabled()
            && handler.getCursorStack().isEmpty()
            && inputSlotsAreEmpty()
            && hasWantedTradeItems()
            && hasEnoughItemsInInventory(recipe)
            && canReceiveOutput(recipe.getSellItem())) {
            System.out.println("transacting...");
            transact(recipe);
            if (++safeguard > 50) {
                break;
            }
        }
    }

    private boolean hasWantedTradeItems() {
        return (wantedTradeItems.size() > 0);
    }

    private boolean inputSlotsAreEmpty() {
        boolean result =
            handler.getSlot(0).getStack().isEmpty()
                && handler.getSlot(1).getStack().isEmpty()
                && handler.getSlot(2).getStack().isEmpty();
        if (frames % 300 == 0) {
            // Debug output commented out for performance
        }
        return result;
    }

    // Helper method to safely get ItemStack from trade offer
    private ItemStack getFirstBuyItemStack(TradeOffer recipe) {
        try {
            TradedItem firstBuyItem = recipe.getFirstBuyItem();
            if (firstBuyItem != null) {
                // Assume TradedItem has a public field 'itemStack'
                return firstBuyItem.itemStack();
            }
        } catch (Exception e) {
            System.err.println("Error accessing first buy item: " + e.getMessage());
        }
        return ItemStack.EMPTY;
    }

    // Helper method to safely get ItemStack from second buy item
    private ItemStack getSecondBuyItemStack(TradeOffer recipe) {
        try {
            Optional<TradedItem> secondBuyItem = recipe.getSecondBuyItem();
            if (secondBuyItem.isPresent()) {
                // Assume TradedItem has a public field 'itemStack'
                return secondBuyItem.get().itemStack();
            }
        } catch (Exception e) {
            System.err.println("Error accessing second buy item: " + e.getMessage());
        }
        return ItemStack.EMPTY;
    }

    private boolean hasEnoughItemsInInventory(TradeOffer recipe) {
        ItemStack firstItem = getFirstBuyItemStack(recipe);
        if (!firstItem.isEmpty() && !hasEnoughItemsInInventory(firstItem))
            return false;

        ItemStack secondItem = getSecondBuyItemStack(recipe);
        if (!secondItem.isEmpty() && !hasEnoughItemsInInventory(secondItem))
            return false;

        return true;
    }

    private boolean hasEnoughItemsInInventory(ItemStack stack) {
        if (stack.isEmpty()) return true;
        int remaining = stack.getCount();
        for (int i = handler.slots.size() - 36; i < handler.slots.size(); i++) {
            ItemStack invstack = handler.getSlot(i).getStack();
            if (invstack.isEmpty())
                continue;
            if (ItemStack.areItemsAndComponentsEqual(stack, invstack)) {
                remaining -= invstack.getCount();
            }
            if (remaining <= 0)
                return true;
        }
        return false;
    }

    private boolean canReceiveOutput(ItemStack stack) {
        if (stack.isEmpty()) return true;
        int remaining = stack.getCount();
        for (int i = handler.slots.size() - 36; i < handler.slots.size(); i++) {
            ItemStack invstack = handler.getSlot(i).getStack();
            if (invstack.isEmpty()) {
                return true;
            }
            if (ItemStack.areItemsAndComponentsEqual(stack, invstack)
                && stack.getMaxCount() >= stack.getCount() + invstack.getCount()) {
                remaining -= (invstack.getMaxCount() - invstack.getCount());
            }
            if (remaining <= 0)
                return true;
        }
        return false;
    }

    private void transact(TradeOffer recipe) {
        int putback0 = -1, putback1 = -1;

        ItemStack firstItem = getFirstBuyItemStack(recipe);
        if (!firstItem.isEmpty()) {
            putback0 = fillSlot(0, firstItem);
        }

        ItemStack secondItem = getSecondBuyItemStack(recipe);
        if (!secondItem.isEmpty()) {
            putback1 = fillSlot(1, secondItem);
        }

        getslot(2, recipe.getSellItem(), putback0, putback1);
        if (putback0 != -1) {
            slotShiftClick(0);
        }
        if (putback1 != -1) {
            slotShiftClick(1);
        }

        this.onMouseClick(null, 0, 99, SlotActionType.SWAP);
    }

    private int fillSlot(int slot, ItemStack stack) {
        int remaining = stack.getCount();
        if (remaining == 0) return -1;
        for (int i = handler.slots.size() - 36; i < handler.slots.size(); i++) {
            ItemStack invstack = handler.getSlot(i).getStack();
            if (invstack.isEmpty())
                continue;
            boolean needPutBack = false;
            if (ItemStack.areItemsAndComponentsEqual(stack, invstack)) {
                if (stack.getCount() + invstack.getCount() > stack.getMaxCount()) {
                    needPutBack = true;
                }

                remaining -= invstack.getCount();
                slotClick(i);
                slotClick(slot);
            }
            if (needPutBack) {
                slotClick(i);
            }
            if (remaining <= 0)
                return remaining < 0 ? i : -1;
        }
        return -1;
    }

    private void getslot(int slot, ItemStack stack, int... forbidden) {
        slotShiftClick(slot);

        // When looking for an empty slot, don't take one that we want to put some input back to.
        for (int i = handler.slots.size() - 36; i < handler.slots.size(); i++) {
            boolean isForbidden = false;
            for (int f : forbidden) {
                if (i == f)
                    isForbidden = true;
            }
            if (isForbidden)
                continue;
            ItemStack invstack = handler.getSlot(i).getStack();
            if (invstack.isEmpty()) {
                slotClick(i);
                return;
            }
        }
    }

    public void slotClick(int slot) {
        this.onMouseClick(null, slot, 0, SlotActionType.PICKUP);
    }

    private void slotShiftClick(int slot) {
        this.onMouseClick(null, slot, 0, SlotActionType.QUICK_MOVE);
    }
}
